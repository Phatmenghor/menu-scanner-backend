package com.emenu.features.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOwnerSubscriptionRenewRequest {
    
    private UUID newPlanId;
    private Integer customDurationDays;
    
    private BigDecimal paymentAmount;
    private String paymentMethod;
    private String paymentReference;
    private String paymentNotes;
    
    public boolean hasPaymentInfo() {
        return paymentAmount != null;
    }
    
    public boolean isPaymentInfoComplete() {
        return paymentAmount != null && paymentMethod != null;
    }
}