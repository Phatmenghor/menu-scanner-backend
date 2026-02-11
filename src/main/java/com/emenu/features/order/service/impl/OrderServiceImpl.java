package com.emenu.features.order.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.filter.OrderFilterRequest;
import com.emenu.features.order.dto.helper.BusinessOrderPaymentCreateHelper;
import com.emenu.features.order.dto.helper.OrderCreateHelper;
import com.emenu.features.order.dto.helper.OrderItemCreateHelper;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.dto.update.OrderUpdateRequest;
import com.emenu.features.order.mapper.BusinessOrderPaymentMapper;
import com.emenu.features.order.mapper.OrderMapper;
import com.emenu.features.order.models.BusinessOrderPayment;
import com.emenu.features.order.models.Cart;
import com.emenu.features.order.models.Order;
import com.emenu.features.order.models.OrderItem;
import com.emenu.features.order.models.OrderProcessStatus;
import com.emenu.features.order.repository.BusinessOrderPaymentRepository;
import com.emenu.features.order.repository.CartRepository;
import com.emenu.features.order.repository.OrderProcessStatusRepository;
import com.emenu.features.order.repository.OrderRepository;
import com.emenu.features.order.service.OrderService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.generate.OrderNumberGenerator;
import com.emenu.shared.generate.PaymentReferenceGenerator;
import com.emenu.shared.mapper.PaginationMapper;
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
    private final BusinessOrderPaymentRepository paymentRepository;
    private final OrderProcessStatusRepository orderProcessStatusRepository;
    private final OrderMapper orderMapper;
    private final BusinessOrderPaymentMapper paymentMapper;
    private final SecurityUtils securityUtils;
    private final OrderNumberGenerator orderNumberGenerator;
    private final PaymentReferenceGenerator paymentReferenceGenerator;
    private final PaginationMapper paginationMapper;

    @Override
    public OrderResponse createOrderFromCart(OrderCreateRequest request) {
        log.info("Creating order from cart for business: {}", request.getBusinessId());

        User currentUser = securityUtils.getCurrentUser();

        Order order = createBaseOrder(request, currentUser.getId());
        assignProcessStatus(order, request.getBusinessId(), request.getOrderProcessStatusId());
        Order savedOrder = orderRepository.save(order);

        // Create order items from cart summary (frontend) or database cart
        if (request.getCart() != null && request.getCart().getItems() != null && !request.getCart().getItems().isEmpty()) {
            log.info("Creating order items from frontend cart summary");
            createOrderItemsFromCartSummary(savedOrder.getId(), request.getCart());
        } else {
            log.info("Creating order items from database cart");
            Cart cart = cartRepository.findByUserIdAndBusinessIdWithItems(currentUser.getId(), request.getBusinessId())
                    .orElseThrow(() -> new ValidationException("Cart is empty or not found"));

            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                throw new ValidationException("Cannot create order from empty cart");
            }

            createOrderItemsFromCart(savedOrder.getId(), cart);
        }

        createPaymentRecord(savedOrder);
        clearCartAfterOrder(currentUser.getId(), request.getBusinessId());

        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
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

        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Order> page = orderRepository.findAll(pageable);
        return orderMapper.toPaginationResponse(page, paginationMapper);
    }

    @Override
    public OrderResponse updateOrder(UUID orderId, OrderUpdateRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!currentUser.getBusinessId().equals(order.getBusinessId())) {
            throw new ValidationException("You can only update orders for your business");
        }

        if (request.getOrderProcessStatusId() != null) {
            // Validate that the status exists and belongs to the business
            OrderProcessStatus processStatus = orderProcessStatusRepository
                    .findById(request.getOrderProcessStatusId())
                    .orElseThrow(() -> new NotFoundException("Order process status not found"));

            if (!processStatus.getBusinessId().equals(currentUser.getBusinessId())) {
                throw new ValidationException("Order process status does not belong to your business");
            }

            order.updateStatus(request.getOrderProcessStatusId());
        }

        // Update delivery address snapshot if provided
        if (request.getDeliveryAddress() != null) {
            var addr = request.getDeliveryAddress();
            String addressSnapshot = String.format("%s, %s, %s, %s",
                    addr.getVillage() != null ? addr.getVillage() : "",
                    addr.getCommune() != null ? addr.getCommune() : "",
                    addr.getDistrict() != null ? addr.getDistrict() : "",
                    addr.getProvince() != null ? addr.getProvince() : "")
                    .replaceAll("^, |, $", "");
            order.setDeliveryAddressSnapshot(addressSnapshot);
        }

        // Update delivery option snapshot if provided
        if (request.getDeliveryOption() != null) {
            var option = request.getDeliveryOption();
            order.setDeliveryOptionName(option.getName());
            order.setDeliveryOptionDescription(option.getDescription());
            order.setDeliveryFee(option.getPrice());

            // Recalculate total with new delivery fee
            order.setTotalAmount(order.getSubtotal().add(option.getPrice()));
        }

        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getCustomerNote() != null) {
            order.setCustomerNote(request.getCustomerNote());
        }
        if (request.getBusinessNote() != null) {
            order.setBusinessNote(request.getBusinessNote());
        }

        Order updatedOrder = orderRepository.save(order);

        log.info("Order updated: {}", orderId);
        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    public OrderResponse deleteOrder(UUID orderId) {
        User currentUser = securityUtils.getCurrentUser();

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!currentUser.getBusinessId().equals(order.getBusinessId())) {
            throw new ValidationException("You can only delete orders for your business");
        }

        order.setIsDeleted(true);
        order = orderRepository.save(order);

        log.info("Order deleted: {}", orderId);

        return orderMapper.toResponse(order);
    }

    private Order createBaseOrder(OrderCreateRequest request, UUID customerId) {
        OrderCreateHelper helper = orderMapper.buildOrderHelper(request, customerId, generateOrderNumber());
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

        // Calculate total with delivery fee (use BigDecimal.ZERO if null)
        BigDecimal deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO;
        order.setTotalAmount(subtotal.add(deliveryFee));
        orderRepository.save(order);
    }

    private void createOrderItemsFromCartSummary(UUID orderId, com.emenu.features.order.dto.response.CartSummaryResponse cartSummary) {
        // Use subtotal from frontend cart summary (already calculated)
        BigDecimal subtotal = cartSummary.getSubtotal();

        for (var item : cartSummary.getItems()) {
            OrderItemCreateHelper helper = OrderItemCreateHelper.builder()
                    .orderId(orderId)
                    .productId(item.getProductId())
                    .productSizeId(item.getProductSizeId())
                    .productName(item.getProductName())
                    .productImageUrl(item.getProductImageUrl())
                    .sizeName(item.getSizeName())
                    // Pricing snapshot from frontend cart (with discounts already applied)
                    .currentPrice(item.getCurrentPrice())
                    .finalPrice(item.getFinalPrice())
                    .unitPrice(item.getFinalPrice()) // unitPrice = finalPrice for backward compat
                    .hasPromotion(item.getHasPromotion())
                    // Promotion details
                    .promotionType(item.getPromotionType())
                    .promotionValue(item.getPromotionValue())
                    // Item details
                    .quantity(item.getQuantity())
                    .specialInstructions(item.getSpecialInstructions())
                    .build();

            OrderItem orderItem = orderMapper.createOrderItemFromHelper(helper);
            // Use totalPrice from frontend (already calculated with discounts)
            orderItem.setTotalPrice(item.getTotalPrice());
        }

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setSubtotal(subtotal);

        // Calculate total with delivery fee (use BigDecimal.ZERO if null)
        BigDecimal deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO;
        order.setTotalAmount(subtotal.add(deliveryFee));
        orderRepository.save(order);
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

    private void assignProcessStatus(Order order, UUID businessId, UUID statusId) {
        if (statusId != null) {
            // Validate that the status exists and belongs to the business
            OrderProcessStatus status = orderProcessStatusRepository
                    .findById(statusId)
                    .orElseThrow(() -> new NotFoundException("Order process status not found"));

            if (!status.getBusinessId().equals(businessId)) {
                throw new ValidationException("Order process status does not belong to the specified business");
            }

            order.setOrderProcessStatusId(statusId);
        } else {
            // If no status provided, use the first status for the business
            orderProcessStatusRepository.findByBusinessIdOrderByCreatedAtAsc(businessId)
                    .stream()
                    .findFirst()
                    .ifPresent(first -> order.setOrderProcessStatusId(first.getId()));
        }
    }

    private String generateOrderNumber() {
        return orderNumberGenerator.generateUniqueOrderNumber(orderRepository::existsByOrderNumber);
    }
}
