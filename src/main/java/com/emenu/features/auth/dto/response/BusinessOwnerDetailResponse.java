package com.emenu.features.auth.dto.response;

import com.emenu.enums.user.BusinessStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BusinessOwnerDetailResponse {
    
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
    private String subscriptionStatus; // ACTIVE, EXPIRED, EXPIRING_SOON
    private Boolean autoRenew;
    private Boolean isExpiringSoon; // Within 7 days
    
    // Payment Summary
    private BigDecimal totalPaid;
    private BigDecimal totalPending;
    private Integer totalPayments;
    private Integer completedPayments;
    private Integer pendingPayments;
    private String paymentStatus; // PAID, PARTIALLY_PAID, UNPAID, PENDING
    private LocalDateTime lastPaymentDate;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}