package com.emenu.features.order.service;

import com.emenu.features.order.dto.filter.OrderPaymentFilterRequest;
import com.emenu.features.order.dto.response.OrderPaymentResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface OrderPaymentService {
    PaginationResponse<OrderPaymentResponse> getAllPayments(OrderPaymentFilterRequest filter);
    OrderPaymentResponse getPaymentById(UUID id);
    OrderPaymentResponse getPaymentByOrderId(UUID orderId);
}
