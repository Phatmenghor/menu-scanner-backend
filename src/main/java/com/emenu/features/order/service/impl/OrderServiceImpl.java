package com.emenu.features.order.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.filter.OrderFilterRequest;
import com.emenu.features.order.dto.helper.OrderPaymentCreateHelper;
import com.emenu.features.order.dto.helper.OrderCreateHelper;
import com.emenu.features.order.dto.helper.OrderItemCreateHelper;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.dto.update.OrderUpdateRequest;
import com.emenu.features.order.mapper.OrderPaymentMapper;
import com.emenu.features.order.mapper.OrderMapper;
import com.emenu.features.order.models.OrderPayment;
import com.emenu.features.order.models.Cart;
import com.emenu.features.order.models.Order;
import com.emenu.features.order.models.OrderItem;
import com.emenu.features.order.models.OrderProcessStatus;
import com.emenu.features.order.repository.OrderPaymentRepository;
import com.emenu.features.order.repository.CartRepository;
import com.emenu.features.order.repository.OrderProcessStatusRepository;
import com.emenu.features.order.repository.OrderRepository;
import com.emenu.features.order.service.OrderService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.generate.ReferenceNumberGenerator;
import com.emenu.shared.generate.PaymentReferenceGenerator;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.pagination.PaginationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final OrderPaymentRepository paymentRepository;
    private final OrderProcessStatusRepository orderProcessStatusRepository;
    private final OrderMapper orderMapper;
    private final OrderPaymentMapper paymentMapper;
    private final SecurityUtils securityUtils;
    private final ReferenceNumberGenerator referenceNumberGenerator;
    private final PaymentReferenceGenerator paymentReferenceGenerator;
    private final PaginationMapper paginationMapper;

    @Override
    public OrderResponse createOrderFromCart(OrderCreateRequest request) {
        log.info("Creating order from cart for business: {}", request.getBusinessId());

        User currentUser = securityUtils.getCurrentUser();

        Order order = createBaseOrder(request, currentUser.getId());
        assignProcessStatus(order, request.getBusinessId(), request.getOrderProcessStatusName());
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
    public PaginationResponse<OrderResponse> getCustomerOrderHistory(OrderFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();
        filter.setBusinessId(null);  // Clear any business filter for customer orders

        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Order> page = orderRepository.findByCustomerIdAndIsDeletedFalseOrderByCreatedAtDesc(currentUser.getId(), pageable);
        return orderMapper.toPaginationResponse(page, paginationMapper);
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

        if (request.getOrderProcessStatusName() != null) {
            // Validate that the status exists and belongs to the business
            OrderProcessStatus processStatus = orderProcessStatusRepository
                    .findByNameAndBusinessIdAndIsDeletedFalse(request.getOrderProcessStatusName(), currentUser.getBusinessId())
                    .orElseThrow(() -> new NotFoundException("Order process status not found: " + request.getOrderProcessStatusName()));

            // Store the status name as snapshot (not ID) for history preservation
            order.updateStatus(request.getOrderProcessStatusName());
        }

        ObjectMapper objectMapper = new ObjectMapper();

        // Update delivery address snapshot if provided
        if (request.getDeliveryAddress() != null) {
            try {
                order.setDeliveryAddressSnapshot(objectMapper.writeValueAsString(request.getDeliveryAddress()));
            } catch (Exception e) {
                log.warn("Failed to serialize delivery address snapshot", e);
            }
        }

        // Update delivery option snapshot if provided
        if (request.getDeliveryOption() != null) {
            try {
                order.setDeliveryOptionSnapshot(objectMapper.writeValueAsString(request.getDeliveryOption()));
            } catch (Exception e) {
                log.warn("Failed to serialize delivery option snapshot", e);
            }
            order.setDeliveryFee(request.getDeliveryOption().getPrice());

            // Recalculate total with new delivery fee
            order.setTotalAmount(order.getSubtotal().add(request.getDeliveryOption().getPrice()));
        }

        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());
        }
        if (request.getCustomerNote() != null) {
            order.setCustomerNote(request.getCustomerNote());
        }
        if (request.getBusinessNote() != null) {
            order.setBusinessNote(request.getBusinessNote());
        }

        // Update order items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            log.info("Updating order items for order: {}", orderId);
            // Clear existing items - cascade delete will handle cleanup
            order.getItems().clear();

            // Create new items from the request
            for (OrderItemCreateHelper itemHelper : request.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrderId(orderId);
                item.setProductId(itemHelper.getProductId());
                item.setProductSizeId(itemHelper.getProductSizeId());
                item.setProductName(itemHelper.getProductName());
                item.setProductImageUrl(itemHelper.getProductImageUrl());
                item.setSizeName(itemHelper.getSizeName());
                item.setCurrentPrice(itemHelper.getCurrentPrice());
                item.setFinalPrice(itemHelper.getFinalPrice());
                item.setUnitPrice(itemHelper.getUnitPrice());
                item.setQuantity(itemHelper.getQuantity());
                item.setTotalPrice(itemHelper.getFinalPrice().multiply(new BigDecimal(itemHelper.getQuantity())));
                item.setHasPromotion(itemHelper.getHasPromotion());
                item.setPromotionType(itemHelper.getPromotionType());
                item.setPromotionValue(itemHelper.getPromotionValue());
                item.setPromotionFromDate(itemHelper.getPromotionFromDate());
                item.setPromotionToDate(itemHelper.getPromotionToDate());
                item.setSpecialInstructions(itemHelper.getSpecialInstructions());
                item.setOrder(order);

                order.getItems().add(item);
            }

            // Recalculate subtotal from items
            BigDecimal newSubtotal = order.getItems().stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setSubtotal(newSubtotal);
        }

        // Full update fields
        if (request.getDiscountAmount() != null) {
            order.setDiscountAmount(request.getDiscountAmount());
        }
        if (request.getTaxAmount() != null) {
            order.setTaxAmount(request.getTaxAmount());
        }
        if (request.getDeliveryFee() != null && request.getDeliveryOption() == null) {
            // Only update delivery fee directly if delivery option is not provided
            order.setDeliveryFee(request.getDeliveryFee());
        }

        // Recalculate total amount if any pricing fields are updated or items changed
        if (request.getItems() != null || request.getDiscountAmount() != null || request.getTaxAmount() != null ||
            (request.getDeliveryFee() != null && request.getDeliveryOption() == null)) {
            BigDecimal subtotal = order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO;
            BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal delivery = order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO;
            BigDecimal tax = order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO;
            order.setTotalAmount(subtotal.subtract(discount).add(delivery).add(tax));
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
        BigDecimal discountAmount = BigDecimal.ZERO;

        for (var cartItem : cart.getItems()) {
            OrderItemCreateHelper helper = orderMapper.buildOrderItemHelperFromCartItem(cartItem, orderId);
            OrderItem orderItem = orderMapper.createOrderItemFromHelper(helper);
            orderItem.calculateTotalPrice();
            subtotal = subtotal.add(orderItem.getTotalPrice());
            // Accumulate discount = base price - final price per item * quantity
            BigDecimal itemDiscount = cartItem.getCurrentPrice().subtract(cartItem.getFinalPrice())
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            if (itemDiscount.compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = discountAmount.add(itemDiscount);
            }
        }

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);

        BigDecimal deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal taxAmount = order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO;
        order.setTotalAmount(subtotal.subtract(discountAmount).add(deliveryFee).add(taxAmount));
        orderRepository.save(order);
    }

    private void createOrderItemsFromCartSummary(UUID orderId, com.emenu.features.order.dto.response.CartSummaryResponse cartSummary) {
        BigDecimal subtotal = cartSummary.getSubtotal() != null ? cartSummary.getSubtotal() : BigDecimal.ZERO;
        BigDecimal discountAmount = cartSummary.getTotalDiscount() != null ? cartSummary.getTotalDiscount() : BigDecimal.ZERO;

        for (var item : cartSummary.getItems()) {
            OrderItemCreateHelper helper = OrderItemCreateHelper.builder()
                    .orderId(orderId)
                    .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                    .productSizeId(item.getProduct() != null ? item.getProduct().getSizeId() : null)
                    .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                    .productImageUrl(item.getProduct() != null ? item.getProduct().getImageUrl() : null)
                    .sizeName(item.getProduct() != null ? item.getProduct().getSizeName() : null)
                    .currentPrice(item.getCurrentPrice())
                    .finalPrice(item.getFinalPrice())
                    .unitPrice(item.getFinalPrice())
                    .hasPromotion(item.getHasActivePromotion())
                    .promotionType(item.getPromotionType())
                    .promotionValue(item.getPromotionValue())
                    .promotionFromDate(item.getPromotionFromDate())
                    .promotionToDate(item.getPromotionToDate())
                    .quantity(item.getQuantity())
                    .build();

            OrderItem orderItem = orderMapper.createOrderItemFromHelper(helper);
            orderItem.setTotalPrice(item.getTotalPrice());
        }

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);

        BigDecimal deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal taxAmount = order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO;
        order.setTotalAmount(subtotal.subtract(discountAmount).add(deliveryFee).add(taxAmount));
        orderRepository.save(order);
    }

    private void createPaymentRecord(Order order) {
        BigDecimal subtotal = order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO;
        BigDecimal discountAmount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal taxAmount = order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO;

        OrderPaymentCreateHelper helper = OrderPaymentCreateHelper.builder()
                .businessId(order.getBusinessId())
                .orderId(order.getId())
                .referenceNumber(paymentReferenceGenerator.generateUniqueReference())
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .deliveryFee(deliveryFee)
                .taxAmount(taxAmount)
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .customerPaymentMethod(null)
                .build();
        OrderPayment payment = paymentMapper.createFromHelper(helper);
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

    private void assignProcessStatus(Order order, UUID businessId, String statusName) {
        if (statusName != null && !statusName.isBlank()) {
            // Validate that the status exists and belongs to the business
            OrderProcessStatus status = orderProcessStatusRepository
                    .findByNameAndBusinessIdAndIsDeletedFalse(statusName, businessId)
                    .orElseThrow(() -> new NotFoundException("Order process status not found: " + statusName));

            // Store the status name as snapshot (not ID) for history preservation
            order.setOrderProcessStatusName(statusName);
        } else {
            // If no status provided, use the first status name for the business
            orderProcessStatusRepository.findByBusinessIdOrderByCreatedAtAsc(businessId)
                    .stream()
                    .findFirst()
                    .ifPresent(first -> order.setOrderProcessStatusName(first.getName()));
        }
    }

    private String generateOrderNumber() {
        return referenceNumberGenerator.generateOrderNumber();
    }
}
