package com.emenu.features.subscription.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubscriptionResponse extends BaseAuditResponse {
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
    
    // Display information
    private String displayName;
    
    // Payment information
    private BigDecimal totalPaidAmount;
    private Boolean isFullyPaid;
    private String paymentStatusSummary;
    private Long totalPaymentsCount;
    private Long completedPaymentsCount;
    private Long pendingPaymentsCount;
}