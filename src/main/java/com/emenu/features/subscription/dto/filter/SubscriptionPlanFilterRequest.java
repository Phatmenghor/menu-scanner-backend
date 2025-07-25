package com.emenu.features.subscription.dto.filter;

import com.emenu.enums.sub_scription.SubscriptionPlanStatus;
import com.emenu.shared.dto.BaseFilterRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubscriptionPlanFilterRequest extends BaseFilterRequest {
    private SubscriptionPlanStatus status;
    private List<SubscriptionPlanStatus> statuses;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minDurationDays;
    private Integer maxDurationDays;
    private Boolean publicOnly = false;
    private Boolean freeOnly = false;
}