package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SubscriptionPlanCreateRequest {
    @NotBlank(message = "Plan name is required")
    private String name;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;
    
    @NotNull(message = "Duration in days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;
    
    private Integer maxStaff;
    private Integer maxMenuItems;
    private Integer maxTables;
    private List<String> features;
    private Boolean isActive = true;
    private Boolean isTrial = false;
    private Integer trialDurationDays;
    private Integer sortOrder;
}