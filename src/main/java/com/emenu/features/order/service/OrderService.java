package com.emenu.features.order.service;

import com.emenu.features.order.dto.filter.OrderFilterRequest;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.dto.update.OrderUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    // Customer Operations
    OrderResponse createOrderFromCart(OrderCreateRequest request);
    PaginationResponse<OrderResponse> getCustomerOrderHistory(OrderFilterRequest filter);
    OrderResponse getOrderById(UUID orderId);

    // Business Operations
    PaginationResponse<OrderResponse> getAllOrders(OrderFilterRequest filter);
    OrderResponse updateOrder(UUID orderId, OrderUpdateRequest request);
    OrderResponse deleteOrder(UUID orderId);
}
