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
    private Boolean createPayment = false;
    private String paymentImageUrl;
    private BigDecimal paymentAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private String paymentReferenceNumber;
    private String paymentNotes;

    // Helper methods
    public boolean shouldCreatePayment() {
        return Boolean.TRUE.equals(createPayment) && paymentAmount != null && 
               paymentAmount.compareTo(BigDecimal.ZERO) > 0 && paymentMethod != null;
    }
}