package com.emenu.features.subscription.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionCancelRequest {
    private String reason;
    private String notes;
    
    // ✅ NEW: Payment handling when cancelling
    private Boolean clearPayments = false; // If true, marks all related payments as cancelled
    private Boolean createRefundRecord = false; // If true, creates refund payment record
    private BigDecimal refundAmount; // Refund amount if createRefundRecord is true
    private String refundNotes;
    
    // ✅ Helper methods
    public boolean shouldClearPayments() {
        return Boolean.TRUE.equals(clearPayments);
    }
    
    public boolean shouldCreateRefundRecord() {
        return Boolean.TRUE.equals(createRefundRecord) && refundAmount != null && 
               refundAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}