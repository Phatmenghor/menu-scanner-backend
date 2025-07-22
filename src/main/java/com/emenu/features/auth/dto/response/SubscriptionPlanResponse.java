package com.emenu.features.auth.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SubscriptionPlanResponse {
    private UUID id;
    private String name;
    private String displayName;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private Integer maxStaff;
    private Integer maxMenuItems;
    private Integer maxTables;
    private List<String> features;
    
    private Boolean isActive;
    private Boolean isDefault;
    private Boolean isCustom;
    private Boolean isTrial;
    private Integer trialDurationDays;
    private Integer sortOrder;
    
    // Computed fields
    private String pricingDisplay;
    private Boolean isFree;
    private Boolean isUnlimitedStaff;
    private Boolean isUnlimitedMenuItems;
    private Boolean isUnlimitedTables;
    
    // Statistics
    private Long activeSubscriptionsCount;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}