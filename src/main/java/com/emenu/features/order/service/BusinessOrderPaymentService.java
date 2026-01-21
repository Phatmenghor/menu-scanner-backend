package com.emenu.features.order.service;

import com.emenu.features.order.dto.filter.BusinessOrderPaymentFilterRequest;
import com.emenu.features.order.dto.response.BusinessOrderPaymentResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface BusinessOrderPaymentService {
    PaginationResponse<BusinessOrderPaymentResponse> getAllPayments(BusinessOrderPaymentFilterRequest filter);
    BusinessOrderPaymentResponse getPaymentById(UUID id);
    BusinessOrderPaymentResponse getPaymentByOrderId(UUID orderId);
}
