
package com.emenu.features.services.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PlanResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String billingCycle;
    private Integer maxUsers;
    private Integer maxMenus;
    private Integer maxOrdersPerMonth;
    private Boolean hasAnalytics;
    private Boolean hasReports;
    private Boolean hasCustomDomain;
    private Boolean isActive;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
