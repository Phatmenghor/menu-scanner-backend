package com.emenu.features.subscription.dto.response;

import com.emenu.enums.sub_scription.SubscriptionPlanStatus;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubscriptionPlanResponse extends BaseAuditResponse {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private SubscriptionPlanStatus status;
    private String pricingDisplay;
    private Long activeSubscriptionsCount;
}