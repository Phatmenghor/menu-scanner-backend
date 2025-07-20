package com.emenu.features.subscription.dto.resposne;

import com.emenu.enums.SubscriptionPlan;
import com.emenu.enums.SubscriptionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionResponse {
    
    private UUID id;
    private UUID businessId;
    private String businessName;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double monthlyPrice;
    private String billingCycle;
    private Boolean autoRenew;
    private Boolean trialPeriod;
    private LocalDateTime trialEndDate;
    
    // Limits
    private Integer maxStaff;
    private Integer maxMenuItems;
    private Integer maxTables;
    
    // Current usage
    private Integer currentStaffCount;
    private Integer currentMenuItems;
    private Integer currentTables;
    
    // Status indicators
    private Boolean isActive;
    private Boolean isInTrial;
    private Integer daysRemaining;
}