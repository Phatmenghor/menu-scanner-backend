package com.emenu.features.auth.dto.update;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SubscriptionPlanUpdateRequest {
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
    private Boolean isTrial;
    private Integer trialDurationDays;
    private Integer sortOrder;
}