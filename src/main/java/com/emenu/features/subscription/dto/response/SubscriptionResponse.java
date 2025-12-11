package com.emenu.features.subscription.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubscriptionResponse extends BaseAuditResponse {
    private UUID businessId;
    private String businessName;
    private UUID planId;
    private String planName;
    private Double planPrice;
    private Integer planDurationDays;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long daysRemaining;
    private Boolean autoRenew;
    private String status;
    private String paymentStatus;
    private Double paymentAmount;
}