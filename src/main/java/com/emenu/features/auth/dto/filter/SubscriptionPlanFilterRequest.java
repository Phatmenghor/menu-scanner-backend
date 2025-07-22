package com.emenu.features.auth.dto.filter;

import com.emenu.enums.sub_scription.SubscriptionPlanStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class SubscriptionPlanFilterRequest {
    private String search;
    private SubscriptionPlanStatus status;
    private List<SubscriptionPlanStatus> statuses;
    private UUID businessId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minDurationDays;
    private Integer maxDurationDays;
    private Boolean publicOnly = false;
    private Boolean freeOnly = false;

    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNo = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer pageSize = 10;

    private String sortBy = "sortOrder";
    private String sortDirection = "ASC";
}