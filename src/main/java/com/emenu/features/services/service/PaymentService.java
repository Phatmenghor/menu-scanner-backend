package com.emenu.features.services.service;

import com.emenu.features.services.dto.filter.PaymentFilterRequest;
import com.emenu.features.services.dto.request.CreatePaymentRequest;
import com.emenu.features.services.dto.response.PaymentResponse;
import com.emenu.features.services.dto.update.UpdatePaymentRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse createPayment(CreatePaymentRequest request);
    PaymentResponse getPayment(UUID id);
    PaymentResponse updatePayment(UUID id, UpdatePaymentRequest request);
    PaymentResponse refundPayment(UUID id);
    PaginationResponse<PaymentResponse> listPayments(PaymentFilterRequest filter);
    PaginationResponse<PaymentResponse> getCurrentUserPayments(PaymentFilterRequest filter);
}