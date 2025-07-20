package com.emenu.features.auth.dto.response;

import com.emenu.enums.SubscriptionPlan;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionResponse {
    
    private UUID id;
    private UUID businessId;
    private String businessName;
    private SubscriptionPlan plan;
    private String planDisplayName;
    private Double planPrice;
    private Integer planDurationDays;
    private Integer maxStaff;
    private Integer maxMenuItems;
    private Integer maxTables;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean isExpired;
    private Long daysRemaining;
    private Boolean autoRenew;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Usage statistics
    private Integer currentStaffCount;
    private Integer currentMenuItemsCount;
    private Integer currentTablesCount;
    private Boolean canAddStaff;
    private Boolean canAddMenuItem;
    private Boolean canAddTable;
}