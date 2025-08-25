package com.emenu.features.subscription.dto.filter;

import com.emenu.enums.sub_scription.SubscriptionPlanStatus;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubscriptionPlanFilterRequest extends BaseFilterRequest {
    private List<SubscriptionPlanStatus> statuses;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minDurationDays;
    private Integer maxDurationDays;
    private Boolean freeOnly = false;
}