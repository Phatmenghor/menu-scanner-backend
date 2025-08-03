package com.emenu.features.payment.service;

import com.emenu.features.payment.dto.filter.BusinessOrderPaymentFilterRequest;
import com.emenu.features.payment.dto.response.BusinessOrderPaymentResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface BusinessOrderPaymentService {
    PaginationResponse<BusinessOrderPaymentResponse> getAllPayments(BusinessOrderPaymentFilterRequest filter);
    BusinessOrderPaymentResponse getPaymentById(UUID id);
    BusinessOrderPaymentResponse getPaymentByOrderId(UUID orderId);
}
