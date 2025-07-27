package com.emenu.features.subdomain.dto.response;

import com.emenu.enums.subdomain.SubdomainStatus;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubdomainResponse extends BaseAuditResponse {
    private String subdomain;
    private UUID businessId;
    private String businessName;
    private SubdomainStatus status;
    private LocalDateTime lastAccessed;
    private Long accessCount;
    private String notes;
    
    // Computed fields
    private String fullDomain;
    private String fullUrl;
    private Boolean isAccessible;
    private Boolean canAccess;
    private Boolean hasActiveSubscription;
    
    // Business subscription info
    private String currentSubscriptionPlan;
    private Long subscriptionDaysRemaining;
}