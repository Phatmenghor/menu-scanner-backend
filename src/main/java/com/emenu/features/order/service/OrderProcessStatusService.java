package com.emenu.features.order.service;

import com.emenu.features.order.dto.filter.OrderProcessStatusFilterRequest;
import com.emenu.features.order.dto.request.OrderProcessStatusCreateRequest;
import com.emenu.features.order.dto.response.OrderProcessStatusResponse;
import com.emenu.features.order.dto.update.OrderProcessStatusUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface OrderProcessStatusService {

    OrderProcessStatusResponse createOrderProcessStatus(OrderProcessStatusCreateRequest request);

    PaginationResponse<OrderProcessStatusResponse> getAllOrderProcessStatuses(OrderProcessStatusFilterRequest filter);

    List<OrderProcessStatusResponse> getBusinessOrderProcessStatuses(UUID businessId);

    OrderProcessStatusResponse getOrderProcessStatusById(UUID id);

    OrderProcessStatusResponse updateOrderProcessStatus(UUID id, OrderProcessStatusUpdateRequest request);

    OrderProcessStatusResponse deleteOrderProcessStatus(UUID id);
}
