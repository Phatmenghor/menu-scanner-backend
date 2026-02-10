package com.emenu.features.order.service;

import com.emenu.features.order.dto.filter.OrderFilterRequest;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.dto.update.OrderStatusUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    // Customer Operations
    OrderResponse createOrderFromCart(OrderCreateRequest request);
    List<OrderResponse> getCustomerOrderHistory();
    OrderResponse getOrderById(UUID orderId);

    // Business Operations
    PaginationResponse<OrderResponse> getAllOrders(OrderFilterRequest filter);
    List<OrderResponse> getBusinessOrders(UUID businessId);
    OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request);
}
