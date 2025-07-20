package com.emenu.features.subscription.service;

import com.emenu.features.subscription.dto.request.PaymentCreateRequest;
import com.emenu.features.subscription.dto.resposne.PaymentResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    
    PaymentResponse createPayment(PaymentCreateRequest request);
    PaymentResponse getPaymentById(UUID id);
    PaginationResponse<PaymentResponse> getPaymentsBySubscription(UUID subscriptionId, int pageNo, int pageSize);
    List<PaymentResponse> getOverduePayments();
    PaymentResponse processPayment(UUID paymentId, String transactionId);
    PaymentResponse refundPayment(UUID paymentId, Double amount);
    void markPaymentAsFailed(UUID paymentId, String reason);
}