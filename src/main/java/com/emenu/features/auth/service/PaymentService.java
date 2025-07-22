package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.PaymentFilterRequest;
import com.emenu.features.auth.dto.request.PaymentCreateRequest;
import com.emenu.features.auth.dto.request.PaymentProcessRequest;
import com.emenu.features.auth.dto.response.PaymentResponse;
import com.emenu.features.auth.dto.response.PaymentSummaryResponse;
import com.emenu.features.auth.dto.update.PaymentUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentService {
    
    // CRUD Operations
    PaymentResponse recordPayment(PaymentCreateRequest request);
    PaginationResponse<PaymentResponse> getPayments(PaymentFilterRequest filter);
    PaymentResponse getPaymentById(UUID id);
    PaymentResponse updatePayment(UUID id, PaymentUpdateRequest request);
    PaymentResponse deletePayment(UUID id);
    
    // Payment Processing Operations
    PaymentResponse completePayment(UUID paymentId, PaymentProcessRequest request);
    PaymentResponse failPayment(UUID paymentId, PaymentProcessRequest request);
    PaymentResponse refundPayment(UUID paymentId, PaymentProcessRequest request);
    PaymentResponse cancelPayment(UUID paymentId, PaymentProcessRequest request);
    
    // Business Payment Operations
    List<PaymentResponse> getBusinessPayments(UUID businessId);
    PaginationResponse<PaymentResponse> getBusinessPaymentsPaginated(UUID businessId, PaymentFilterRequest filter);
    BigDecimal getBusinessTotalPaidAmount(UUID businessId);
    PaymentSummaryResponse getBusinessPaymentSummary(UUID businessId);
    
    // Plan Payment Operations
    List<PaymentResponse> getPlanPayments(UUID planId);
    long getPlanPaymentCount(UUID planId);
    BigDecimal getPlanTotalRevenue(UUID planId);
    
    // Payment Analytics & Reports
    PaymentSummaryResponse getOverallPaymentSummary();
    PaymentSummaryResponse getPaymentSummaryByDateRange(String startDate, String endDate);
    List<PaymentResponse> getOverduePayments();
    List<PaymentResponse> getRecentPayments(int limit);
    
    // Revenue Analytics
    BigDecimal getTotalRevenue();
    BigDecimal getMonthlyRevenue();
    BigDecimal getYearlyRevenue();
    BigDecimal getRevenueByDateRange(String startDate, String endDate);
    
    // Count Analytics
    long getTotalPaymentsCount();
    long getPendingPaymentsCount();
    long getCompletedPaymentsCount();
    long getFailedPaymentsCount();
    long getOverduePaymentsCount();
    
    // Utility Operations
    String generateReferenceNumber();
    boolean isReferenceNumberUnique(String referenceNumber);
    PaymentResponse getPaymentByReferenceNumber(String referenceNumber);
    
    // Subscription Integration
    void processSubscriptionPayment(UUID subscriptionId, UUID businessId, UUID planId);
    void extendSubscriptionAfterPayment(UUID paymentId);
}