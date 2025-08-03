package com.emenu.features.order.service;

import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.dto.update.OrderStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    
    // Customer Operations
    OrderResponse createOrderFromCart(OrderCreateRequest request);
    List<OrderResponse> getCustomerOrderHistory();
    OrderResponse getOrderById(UUID orderId);
    
    // Business Operations  
    List<OrderResponse> getBusinessOrders(UUID businessId);
    OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request);
    
    // Internal
    void clearCartAfterOrder(UUID customerId, UUID businessId);
}