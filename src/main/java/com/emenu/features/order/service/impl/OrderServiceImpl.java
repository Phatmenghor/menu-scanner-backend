package com.emenu.features.order.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.filter.OrderFilterRequest;
import com.emenu.features.order.dto.helper.BusinessOrderPaymentCreateHelper;
import com.emenu.features.order.dto.helper.OrderCreateHelper;
import com.emenu.features.order.dto.helper.OrderItemCreateHelper;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.request.POSOrderCreateRequest;
import com.emenu.features.order.dto.request.POSOrderItemRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.dto.update.OrderStatusUpdateRequest;
import com.emenu.features.order.mapper.BusinessOrderPaymentMapper;
import com.emenu.features.order.mapper.OrderMapper;
import com.emenu.features.order.models.BusinessOrderPayment;
import com.emenu.features.order.models.Cart;
import com.emenu.features.order.models.Order;
import com.emenu.features.order.models.OrderItem;
import com.emenu.features.order.repository.BusinessOrderPaymentRepository;
import com.emenu.features.order.repository.CartRepository;
import com.emenu.features.order.repository.OrderRepository;
import com.emenu.features.order.service.OrderService;
import com.emenu.features.main.models.Product;
import com.emenu.features.main.models.ProductSize;
import com.emenu.features.main.repository.ProductRepository;
import com.emenu.features.main.repository.ProductSizeRepository;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.generate.OrderNumberGenerator;
import com.emenu.shared.generate.PaymentReferenceGenerator;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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
    private final BusinessOrderPaymentMapper paymentMapper;
    private final SecurityUtils securityUtils;
    private final OrderNumberGenerator orderNumberGenerator;
    private final PaymentReferenceGenerator paymentReferenceGenerator;
    private final com.emenu.shared.mapper.PaginationMapper paginationMapper;

    @Override
    public OrderResponse createOrderFromCart(OrderCreateRequest request) {
        log.info("Creating order from cart for business: {}", request.getBusinessId());

        User currentUser = securityUtils.getCurrentUser();

        Cart cart = cartRepository.findByUserIdAndBusinessIdWithItems(currentUser.getId(), request.getBusinessId())
                .orElseThrow(() -> new ValidationException("Cart is empty or not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ValidationException("Cannot create order from empty cart");
        }

        Order order = createBaseOrder(request, currentUser.getId());
        Order savedOrder = orderRepository.save(order);

        createOrderItemsFromCart(savedOrder.getId(), cart);
        createPaymentRecord(savedOrder);
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

        OrderCreateHelper helper = orderMapper.buildGuestOrderHelper(request, generateOrderNumber());
        Order order = orderMapper.createFromHelper(helper);
        Order savedOrder = orderRepository.save(order);

        createPaymentRecord(savedOrder);

        log.info("Guest order created successfully: {}", savedOrder.getOrderNumber());
        return getOrderById(savedOrder.getId());
    }

    @Override
    public OrderResponse createPOSOrder(POSOrderCreateRequest request) {
        log.info("Creating POS order for customer: {}", request.getCustomerPhone());

        User currentUser = securityUtils.getCurrentUser();
        validateUserBusinessAssociation(currentUser);

        BigDecimal subtotal = calculatePOSOrderTotal(request.getItems());
        OrderCreateHelper helper = orderMapper.buildPOSOrderHelper(
                request,
                currentUser.getBusinessId(),
                generateOrderNumber(),
                subtotal
        );

        Order order = orderMapper.createFromHelper(helper);
        Order savedOrder = orderRepository.save(order);

        createPOSOrderItems(savedOrder.getId(), request.getItems());

        BusinessOrderPaymentCreateHelper paymentHelper = BusinessOrderPaymentCreateHelper.builder()
                .businessId(savedOrder.getBusinessId())
                .orderId(savedOrder.getId())
                .referenceNumber(paymentReferenceGenerator.generateUniqueReference())
                .amount(savedOrder.getTotalAmount())
                .paymentMethod(savedOrder.getPaymentMethod())
                .customerPaymentMethod(request.getCustomerPaymentMethod())
                .build();
        BusinessOrderPayment payment = paymentMapper.createFromHelper(paymentHelper);
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

        if (currentUser.isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.getBusinessId());
        }

        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Order> page = orderRepository.findAll(pageable);
        return orderMapper.toPaginationResponse(page, paginationMapper);
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

        if (!currentUser.getBusinessId().equals(order.getBusinessId())) {
            throw new ValidationException("You can only update orders for your business");
        }

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

    private Order createBaseOrder(OrderCreateRequest request, UUID customerId) {
        OrderCreateHelper helper = orderMapper.buildBaseOrderHelper(request, customerId, generateOrderNumber());
        return orderMapper.createFromHelper(helper);
    }

    private void createOrderItemsFromCart(UUID orderId, Cart cart) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (var cartItem : cart.getItems()) {
            OrderItemCreateHelper helper = orderMapper.buildOrderItemHelperFromCartItem(cartItem, orderId);
            OrderItem orderItem = orderMapper.createOrderItemFromHelper(helper);
            orderItem.calculateTotalPrice();
            subtotal = subtotal.add(orderItem.getTotalPrice());
        }

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal.add(order.getDeliveryFee()));
        orderRepository.save(order);
    }

    private void createPOSOrderItems(UUID orderId, List<POSOrderItemRequest> itemRequests) {
        for (POSOrderItemRequest itemRequest : itemRequests) {
            Product product = productRepository.findByIdAndIsDeletedFalse(itemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + itemRequest.getProductId()));

            String sizeName;
            BigDecimal unitPrice;

            if (itemRequest.getProductSizeId() != null) {
                ProductSize productSize = productSizeRepository.findById(itemRequest.getProductSizeId())
                        .orElseThrow(() -> new NotFoundException("Product size not found"));
                sizeName = productSize.getName();
                unitPrice = productSize.getFinalPrice();
            } else {
                sizeName = "Standard";
                unitPrice = product.getFinalPrice();
            }

            OrderItemCreateHelper helper = orderMapper.buildOrderItemHelperFromProduct(
                    orderId,
                    product,
                    itemRequest.getProductSizeId(),
                    sizeName,
                    unitPrice,
                    itemRequest.getQuantity()
            );

            OrderItem orderItem = orderMapper.createOrderItemFromHelper(helper);
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
        BusinessOrderPaymentCreateHelper helper = BusinessOrderPaymentCreateHelper.builder()
                .businessId(order.getBusinessId())
                .orderId(order.getId())
                .referenceNumber(paymentReferenceGenerator.generateUniqueReference())
                .amount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .customerPaymentMethod(null)
                .build();
        BusinessOrderPayment payment = paymentMapper.createFromHelper(helper);
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
        return orderNumberGenerator.generateUniqueOrderNumber(orderRepository::existsByOrderNumber);
    }
}
