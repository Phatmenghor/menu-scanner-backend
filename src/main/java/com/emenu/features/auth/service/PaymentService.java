package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.PaymentFilterRequest;
import com.emenu.features.auth.dto.request.PaymentCreateRequest;
import com.emenu.features.auth.dto.response.PaymentResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentService {
    
    // CRUD Operations
    PaymentResponse recordPayment(PaymentCreateRequest request);
    PaginationResponse<PaymentResponse> getPayments(PaymentFilterRequest filter);
    PaymentResponse getPaymentById(UUID id);
    
    // Payment Status Management
    PaymentResponse completePayment(UUID paymentId);
    PaymentResponse failPayment(UUID paymentId);
    PaymentResponse refundPayment(UUID paymentId);
    
    // Business Payment Management
    List<PaymentResponse> getBusinessPayments(UUID businessId);
    BigDecimal getTotalPaidAmount(UUID businessId);
    
    // Payment Analytics
    BigDecimal getTotalRevenue();
    BigDecimal getMonthlyRevenue();
    long getPendingPaymentsCount();
}