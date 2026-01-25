package com.emenu.features.order.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.filter.OrderFilterRequest;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.request.POSOrderCreateRequest;
import com.emenu.features.order.dto.request.POSOrderItemRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.dto.update.OrderStatusUpdateRequest;
import com.emenu.features.order.mapper.OrderMapper;
import com.emenu.features.order.models.Cart;
import com.emenu.features.order.models.Order;
import com.emenu.features.order.models.OrderItem;
import com.emenu.features.order.repository.CartRepository;
import com.emenu.features.order.repository.OrderRepository;
import com.emenu.features.order.service.OrderService;
import com.emenu.features.order.models.BusinessOrderPayment;
import com.emenu.features.order.repository.BusinessOrderPaymentRepository;
import com.emenu.features.main.models.Product;
import com.emenu.features.main.models.ProductSize;
import com.emenu.features.main.repository.ProductRepository;
import com.emenu.features.main.repository.ProductSizeRepository;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.generate.PaymentReferenceGenerator;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final BusinessOrderPaymentRepository paymentRepository;
    private final OrderMapper orderMapper;
    private final SecurityUtils securityUtils;
    private final PaymentReferenceGenerator paymentReferenceGenerator;
    private final com.emenu.shared.mapper.PaginationMapper paginationMapper;
    
    private static final AtomicLong orderCounter = new AtomicLong(System.currentTimeMillis() % 10000);

    @Override
    public OrderResponse createOrderFromCart(OrderCreateRequest request) {
        log.info("Creating order from cart for business: {}", request.getBusinessId());
        
        User currentUser = securityUtils.getCurrentUser();
        
        // Get cart with items
        Cart cart = cartRepository.findByUserIdAndBusinessIdWithItems(currentUser.getId(), request.getBusinessId())
                .orElseThrow(() -> new ValidationException("Cart is empty or not found"));
        
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ValidationException("Cannot create order from empty cart");
        }
        
        // Create order
        Order order = createBaseOrder(request, currentUser.getId());
        Order savedOrder = orderRepository.save(order);
        
        // Create order items from cart items
        createOrderItemsFromCart(savedOrder.getId(), cart);
        
        // Create payment record
        createPaymentRecord(savedOrder);
        
        // Clear cart after successful order
        clearCartAfterOrder(currentUser.getId(), request.getBusinessId());
        
        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
        return getOrderById(savedOrder.getId());
    }

    @Override
    public OrderResponse createGuestOrder(OrderCreateRequest request, List<UUID> cartItemIds) {
        log.info("Creating guest order for business: {}", request.getBusinessId());
        
        if (request.getGuestPhone() == null || request.getGuestPhone().trim().isEmpty()) {
            throw new ValidationException("Phone number is required for guest orders");
        }
        
        // Create guest order
        Order order = createBaseOrder(request, null);
        order.setIsGuestOrder(true);
        order.setGuestPhone(request.getGuestPhone());
        order.setGuestName(request.getGuestName());
        order.setGuestLocation(request.getGuestLocation());
        
        Order savedOrder = orderRepository.save(order);
        
        // Create payment record
        createPaymentRecord(savedOrder);
        
        log.info("Guest order created successfully: {}", savedOrder.getOrderNumber());
        return getOrderById(savedOrder.getId());
    }

    @Override
    public OrderResponse createPOSOrder(POSOrderCreateRequest request) {
        log.info("Creating POS order for customer: {}", request.getCustomerPhone());
        
        User currentUser = securityUtils.getCurrentUser();
        validateUserBusinessAssociation(currentUser);
        
        // Create POS order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setBusinessId(currentUser.getBusinessId());
        order.setGuestPhone(request.getCustomerPhone());
        order.setGuestName(request.getCustomerName());
        order.setGuestLocation(request.getCustomerLocation());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCustomerNote(request.getCustomerNote());
        order.setBusinessNote(request.getBusinessNote());
        order.setIsPosOrder(true);
        order.setIsGuestOrder(true);
        order.setIsPaid(true); // POS orders are paid immediately
        
        // Calculate pricing from items
        BigDecimal subtotal = calculatePOSOrderTotal(request.getItems());
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal);
        
        Order savedOrder = orderRepository.save(order);
        
        // Create order items
        createPOSOrderItems(savedOrder.getId(), request.getItems());
        
        // Create payment record
        BusinessOrderPayment payment = new BusinessOrderPayment(
            savedOrder.getBusinessId(),
            savedOrder.getId(),
            paymentReferenceGenerator.generateUniqueReference(),
            savedOrder.getTotalAmount(),
            savedOrder.getPaymentMethod(),
            request.getCustomerPaymentMethod()
        );
        paymentRepository.save(payment);
        
        log.info("POS order created successfully: {}", savedOrder.getOrderNumber());
        return getOrderById(savedOrder.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrderHistory() {
        User currentUser = securityUtils.getCurrentUser();
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(currentUser.getId());
        return orderMapper.toResponseList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<OrderResponse> getAllOrders(OrderFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();
        
        // Business users can only see their own orders
        if (currentUser.isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.getBusinessId());
        }
        
        // TODO: Implement repository-based filtering
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );
        
        // Page needs repository query method
        return orderMapper.toPaginationResponse(orderPage, paginationMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getBusinessOrders(UUID businessId) {
        List<Order> orders = orderRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
        return orderMapper.toResponseList(orders);
    }

    @Override
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        
        // Security check - only business owner can update orders
        if (!currentUser.getBusinessId().equals(order.getBusinessId())) {
            throw new ValidationException("You can only update orders for your business");
        }
        
        // Update status
        switch (request.getStatus()) {
            case CONFIRMED -> order.confirm();
            case DELIVERED -> order.complete();
            case CANCELLED -> order.cancel();
            case REJECTED -> order.reject();
        }
        
        if (request.getBusinessNote() != null) {
            order.setBusinessNote(request.getBusinessNote());
        }
        
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order status updated: {} -> {}", orderId, request.getStatus());
        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getGuestOrdersByPhone(String phone) {
        List<Order> orders = orderRepository.findByGuestPhoneOrderByCreatedAtDesc(phone);
        return orderMapper.toResponseList(orders);
    }

    // Private helper methods
    private Order createBaseOrder(OrderCreateRequest request, UUID customerId) {
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerId(customerId);
        order.setBusinessId(request.getBusinessId());
        order.setDeliveryAddressId(request.getDeliveryAddressId());
        order.setDeliveryOptionId(request.getDeliveryOptionId());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCustomerNote(request.getCustomerNote());
        order.setIsPosOrder(request.getIsPosOrder());
        order.setIsGuestOrder(request.getIsGuestOrder());
        
        return order;
    }

    private void createOrderItemsFromCart(UUID orderId, Cart cart) {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (var cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductSizeId(cartItem.getProductSizeId());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setProductImageUrl(cartItem.getProduct().getMainImageUrl());
            orderItem.setSizeName(cartItem.getSizeName());
            orderItem.setUnitPrice(cartItem.getFinalPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.calculateTotalPrice();
            
            subtotal = subtotal.add(orderItem.getTotalPrice());
        }
        
        // Update order total
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal.add(order.getDeliveryFee()));
        orderRepository.save(order);
    }

    private void createPOSOrderItems(UUID orderId, List<POSOrderItemRequest> itemRequests) {
        for (POSOrderItemRequest itemRequest : itemRequests) {
            Product product = productRepository.findByIdAndIsDeletedFalse(itemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + itemRequest.getProductId()));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setProductSizeId(itemRequest.getProductSizeId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImageUrl(product.getMainImageUrl());
            orderItem.setQuantity(itemRequest.getQuantity());
            
            // Get price from product or size
            if (itemRequest.getProductSizeId() != null) {
                ProductSize productSize = productSizeRepository.findById(itemRequest.getProductSizeId())
                        .orElseThrow(() -> new NotFoundException("Product size not found"));
                orderItem.setSizeName(productSize.getName());
                orderItem.setUnitPrice(productSize.getFinalPrice());
            } else {
                orderItem.setSizeName("Standard");
                orderItem.setUnitPrice(product.getFinalPrice());
            }
            
            orderItem.calculateTotalPrice();
        }
    }

    private BigDecimal calculatePOSOrderTotal(List<POSOrderItemRequest> items) {
        BigDecimal total = BigDecimal.ZERO;
        
        for (POSOrderItemRequest item : items) {
            Product product = productRepository.findByIdAndIsDeletedFalse(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + item.getProductId()));
            
            BigDecimal itemPrice;
            if (item.getProductSizeId() != null) {
                ProductSize productSize = productSizeRepository.findById(item.getProductSizeId())
                        .orElseThrow(() -> new NotFoundException("Product size not found"));
                itemPrice = productSize.getFinalPrice();
            } else {
                itemPrice = product.getFinalPrice();
            }
            
            total = total.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        
        return total;
    }

    private void createPaymentRecord(Order order) {
        BusinessOrderPayment payment = new BusinessOrderPayment(
            order.getBusinessId(),
            order.getId(),
            paymentReferenceGenerator.generateUniqueReference(),
            order.getTotalAmount(),
            order.getPaymentMethod(),
            null // Will be set by business for POS orders
        );
        paymentRepository.save(payment);
    }

    private void clearCartAfterOrder(UUID customerId, UUID businessId) {
        cartRepository.findByUserIdAndBusinessIdAndIsDeletedFalse(customerId, businessId)
                .ifPresent(cart -> {
                    if (cart.getItems() != null) {
                        cart.getItems().clear();
                    }
                    log.info("Cart cleared after order for customer: {} and business: {}", customerId, businessId);
                });
    }

    private void validateUserBusinessAssociation(User user) {
        if (user.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = orderCounter.incrementAndGet() % 10000;
        String orderNumber;
        
        do {
            orderNumber = String.format("ORD-%s-%04d", date, counter);
            counter = (counter + 1) % 10000;
        } while (orderRepository.existsByOrderNumber(orderNumber));
        
        return orderNumber;
    }
}