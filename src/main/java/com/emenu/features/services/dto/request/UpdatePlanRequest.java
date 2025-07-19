package com.emenu.features.services.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatePlanRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String billingCycle;
    private Integer maxUsers;
    private Integer maxMenus;
    private Integer maxOrdersPerMonth;
    private Boolean hasAnalytics;
    private Boolean hasReports;
    private Boolean hasCustomDomain;
    private Boolean isActive;
    private Integer sortOrder;
}
