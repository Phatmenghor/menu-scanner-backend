package com.emenu.features.order.service;

import com.emenu.features.order.dto.filter.OrderFilterRequest;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.request.POSOrderCreateRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.dto.update.OrderStatusUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    
    // Customer Operations
    OrderResponse createOrderFromCart(OrderCreateRequest request);
    OrderResponse createGuestOrder(OrderCreateRequest request, List<UUID> cartItemIds);
    List<OrderResponse> getCustomerOrderHistory();
    OrderResponse getOrderById(UUID orderId);
    
    // POS Operations (for business staff)
    OrderResponse createPOSOrder(POSOrderCreateRequest request);
    
    // Business Operations  
    PaginationResponse<OrderResponse> getAllOrders(OrderFilterRequest filter);
    List<OrderResponse> getBusinessOrders(UUID businessId);
    OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request);
    
    // Guest Operations
    List<OrderResponse> getGuestOrdersByPhone(String phone);
}
