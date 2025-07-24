package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.PaymentFilterRequest;
import com.emenu.features.auth.dto.request.PaymentCreateRequest;
import com.emenu.features.auth.dto.response.PaymentResponse;
import com.emenu.features.auth.dto.update.PaymentUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    
    // Basic CRUD Operations
    PaymentResponse createPayment(PaymentCreateRequest request);
    PaginationResponse<PaymentResponse> getAllPayments(PaymentFilterRequest filter);
    PaymentResponse getPaymentById(UUID id);
    PaymentResponse updatePayment(UUID id, PaymentUpdateRequest request);
    PaymentResponse deletePayment(UUID id);
    
    // Business Operations
    List<PaymentResponse> getBusinessPayments(UUID businessId);
    PaginationResponse<PaymentResponse> getBusinessPaymentsPaginated(UUID businessId, PaymentFilterRequest filter);
    
    // ✅ ADDED: Subscription Operations
    List<PaymentResponse> getSubscriptionPayments(UUID subscriptionId);
    
    // Status Operations
    PaymentResponse completePayment(UUID id, String notes);
    PaymentResponse cancelPayment(UUID id, String reason);
    
    // Utility Operations
    PaymentResponse getPaymentByReference(String referenceNumber);
    String generateReferenceNumber();
    boolean isReferenceNumberUnique(String referenceNumber);
    
    // Statistics
    long getTotalPaymentsCount();
    long getCompletedPaymentsCount();
    long getPendingPaymentsCount();
    long getBusinessPaymentsCount(UUID businessId);
    
    // ✅ ADDED: Subscription Statistics
    long getSubscriptionPaymentsCount(UUID subscriptionId);
}