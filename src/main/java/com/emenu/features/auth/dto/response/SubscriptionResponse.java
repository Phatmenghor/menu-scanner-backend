package com.emenu.features.auth.dto.response;

import lombok.Data;

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
    private String planDisplayName;
    private Double planPrice;
    private Integer planDurationDays;
    
    // Effective limits (considering custom overrides)
    private Integer effectiveMaxStaff;
    private Integer effectiveMaxMenuItems;
    private Integer effectiveMaxTables;
    private Integer effectiveDurationDays;
    
    // Custom overrides
    private Integer customMaxStaff;
    private Integer customMaxMenuItems;
    private Integer customMaxTables;
    private Integer customDurationDays;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean isExpired;
    private Long daysRemaining;
    private Boolean autoRenew;
    private Boolean isTrial;
    private String notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Usage statistics
    private Integer currentStaffCount;
    private Integer currentMenuItemsCount;
    private Integer currentTablesCount;
    private Boolean canAddStaff;
    private Boolean canAddMenuItem;
    private Boolean canAddTable;
    
    // Display information
    private String displayName;
    private Boolean hasCustomLimits;
}