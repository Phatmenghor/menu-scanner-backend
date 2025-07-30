package com.emenu.features.subscription.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionCancelRequest {
    private String reason;
    private String notes;
    
    // ✅ SIMPLIFIED: When cancelling, automatically handle payments and create refund
    private BigDecimal refundAmount; // Refund amount to be processed
    private String refundNotes;
    
    // ✅ Helper method
    public boolean hasRefundAmount() {
        return refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}