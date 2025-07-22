package com.emenu.features.auth.dto.response;

import com.emenu.enums.sub_scription.SubscriptionPlanStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionPlanResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private SubscriptionPlanStatus status;
    
    // Computed fields
    private String pricingDisplay;
    private Boolean isFree;
    private Boolean isPublic;
    private Boolean isPrivate;
    
    // Statistics
    private Long activeSubscriptionsCount;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}