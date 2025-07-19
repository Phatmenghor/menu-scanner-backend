package com.emenu.features.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePlanRequest {
    @NotBlank
    private String name;
    
    private String description;
    
    @NotNull
    @Positive
    private BigDecimal price;
    
    private String currency = "USD";
    
    @NotBlank
    private String billingCycle; // MONTHLY, YEARLY
    
    private Integer maxUsers;
    private Integer maxMenus;
    private Integer maxOrdersPerMonth;
    private Boolean hasAnalytics = false;
    private Boolean hasReports = false;
    private Boolean hasCustomDomain = false;
    private Integer sortOrder = 0;
}