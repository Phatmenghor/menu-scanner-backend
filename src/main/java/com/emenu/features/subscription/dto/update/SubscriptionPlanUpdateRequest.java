package com.emenu.features.subscription.dto.update;

import com.emenu.enums.sub_scription.SubscriptionPlanStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionPlanUpdateRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private SubscriptionPlanStatus status;
}