package com.emenu.features.auth.dto.response;

import com.emenu.enums.sub_scription.SubscriptionStatus;
import com.emenu.enums.user.BusinessStatus;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessOwnerDetailResponse extends BaseAuditResponse {
    
    // Owner Information
    private UUID ownerId;
    private String ownerUserIdentifier;
    private String ownerEmail;
    private String ownerFullName;
    private String ownerPhone;
    private String ownerAccountStatus;
    
    // Business Information
    private UUID businessId;
    private String businessName;
    private String businessEmail;
    private String businessPhone;
    private String businessAddress;
    private BusinessStatus businessStatus;
    private Boolean isSubscriptionActive;
    private LocalDateTime businessCreatedAt;
    
    // Current Subscription Information
    private UUID currentSubscriptionId;
    private String currentPlanName;
    private BigDecimal currentPlanPrice;
    private Integer currentPlanDurationDays;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;
    private Long daysRemaining;
    private Long daysActive;
    private SubscriptionStatus subscriptionStatus;  // Changed from String to enum
    private Boolean autoRenew;
    private Boolean isExpiringSoon;
    
    // Payment Summary
    private BigDecimal totalPaid;
    private BigDecimal totalPending;
    private Integer totalPayments;
    private Integer completedPayments;
    private Integer pendingPayments;
    private String paymentStatus;
    private LocalDateTime lastPaymentDate;
}


