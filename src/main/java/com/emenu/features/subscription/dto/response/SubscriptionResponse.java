package com.emenu.features.subscription.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionResponse {
    private UUID id;
    private UUID businessId;
    private String businessName;
    
    // Plan information
    private UUID planId;
    private String planName;
    private Double planPrice;
    private Integer planDurationDays;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean isExpired;
    private Long daysRemaining;
    private Boolean autoRenew;
    private String notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Display information
    private String displayName;
    
    // ✅ ADDED: Payment information
    private BigDecimal totalPaidAmount;
    private Boolean isFullyPaid;
    private String paymentStatusSummary;
    private Long totalPaymentsCount;
    private Long completedPaymentsCount;
    private Long pendingPaymentsCount;
}