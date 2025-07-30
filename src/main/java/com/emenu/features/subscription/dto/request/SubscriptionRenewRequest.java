package com.emenu.features.subscription.dto.request;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SubscriptionRenewRequest {
    private UUID newPlanId;
    private Integer customDurationDays;
    
    // Auto-create payment when renewing
    private Boolean createPayment = false; // If true, creates payment record
    private String paymentImageUrl; // Receipt image URL
    private BigDecimal paymentAmount; // Payment amount
    private PaymentMethod paymentMethod; // Required if createPayment is true
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private String paymentReferenceNumber; // Optional, will auto-generate
    private String paymentNotes;

    // Helper methods
    public boolean shouldCreatePayment() {
        return Boolean.TRUE.equals(createPayment) && paymentAmount != null && 
               paymentAmount.compareTo(BigDecimal.ZERO) > 0 && paymentMethod != null;
    }
}