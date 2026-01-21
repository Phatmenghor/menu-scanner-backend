package com.emenu.features.order.service;

import com.emenu.features.order.dto.filter.PaymentFilterRequest;
import com.emenu.features.order.dto.request.PaymentCreateRequest;
import com.emenu.features.order.dto.response.PaymentResponse;
import com.emenu.features.order.dto.update.PaymentUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse createPayment(PaymentCreateRequest request);

    PaginationResponse<PaymentResponse> getAllPayments(PaymentFilterRequest filter);
    PaymentResponse getPaymentById(UUID id);
    PaymentResponse updatePayment(UUID id, PaymentUpdateRequest request);
    PaymentResponse deletePayment(UUID id);
}