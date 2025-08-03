package com.emenu.features.order.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.dto.update.OrderStatusUpdateRequest;
import com.emenu.features.order.mapper.OrderMapper;
import com.emenu.features.order.models.Cart;
import com.emenu.features.order.models.Order;
import com.emenu.features.order.models.OrderItem;
import com.emenu.features.order.models.OrderStatusHistory;
import com.emenu.features.order.repository.CartItemRepository;
import com.emenu.features.order.repository.CartRepository;
import com.emenu.features.order.repository.OrderRepository;
import com.emenu.features.order.service.OrderService;
import com.emenu.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final CartItemRepository cartItemRepository;
    private final OrderMapper orderMapper;
    private final SecurityUtils securityUtils;
    
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
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerId(currentUser.getId());
        order.setBusinessId(request.getBusinessId());
        order.setDeliveryAddressId(request.getDeliveryAddressId());
        order.setDeliveryOptionId(request.getDeliveryOptionId());
        order.setCustomerNote(request.getCustomerNote());
        
        // Calculate pricing
        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal deliveryFee = BigDecimal.ZERO;
        
        // Add delivery fee if delivery option is selected
        if (request.getDeliveryOptionId() != null) {
            // You would fetch delivery option and get its price
            // For now, setting to 0
            deliveryFee = BigDecimal.ZERO;
        }
        
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(subtotal.add(deliveryFee));
        
        Order savedOrder = orderRepository.save(order);
        
        // Create order items from cart items
        for (var cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(savedOrder.getId());
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductSizeId(cartItem.getProductSizeId());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setProductImageUrl(cartItem.getProduct().getMainImageUrl());
            orderItem.setSizeName(cartItem.getSizeName());
            orderItem.setUnitPrice(cartItem.getFinalPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.calculateTotalPrice();
            
            // Save order item (you'd need OrderItemRepository)
        }
        
        // Create status history
        OrderStatusHistory statusHistory = new OrderStatusHistory(
                savedOrder.getId(),
                savedOrder.getStatus(),
                "Order created by customer",
                currentUser.getUserIdentifier()
        );
        // Save status history (you'd need OrderStatusHistoryRepository)
        
        // Clear cart after successful order
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
        
        // Update status based on request
        switch (request.getStatus()) {
            case CONFIRMED -> order.confirm();
            case PREPARING -> order.prepare();
            case READY -> order.markReady();
            case OUT_FOR_DELIVERY -> order.markOutForDelivery();
            case DELIVERED -> order.deliver();
            case CANCELLED -> order.cancel();
            case REJECTED -> order.reject();
        }
        
        if (request.getBusinessNote() != null) {
            order.setBusinessNote(request.getBusinessNote());
        }
        
        Order updatedOrder = orderRepository.save(order);
        
        // Create status history
        OrderStatusHistory statusHistory = new OrderStatusHistory(
                updatedOrder.getId(),
                updatedOrder.getStatus(),
                request.getBusinessNote(),
                currentUser.getUserIdentifier()
        );
        // Save status history
        
        log.info("Order status updated: {} -> {}", orderId, request.getStatus());
        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    public void clearCartAfterOrder(UUID customerId, UUID businessId) {
        cartRepository.findByUserIdAndBusinessIdAndIsDeletedFalse(customerId, businessId)
                .ifPresent(cart -> {
                    // Hard delete all cart items
                    if (cart.getItems() != null) {
                        cart.getItems().forEach(cartItemRepository::delete);
                    }
                    log.info("Cart cleared after order for customer: {} and business: {}", customerId, businessId);
                });
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