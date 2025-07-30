package com.emenu.features.subscription.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SubscriptionCancellationResponse {
    private SubscriptionResponse subscription;
    private Boolean paymentsCleared;
    private Boolean refundCreated;
    private BigDecimal refundAmount;
    
    // Summary info
    private String cancellationSummary;
    private LocalDateTime cancellationDate;
    private String reason;
}