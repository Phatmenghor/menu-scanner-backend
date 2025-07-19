package com.emenu.features.services.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionStatsResponse {
    private Long totalSubscriptions;
    private Long activeSubscriptions;
    private Long expiredSubscriptions;
    private Long cancelledSubscriptions;
    private BigDecimal monthlyRevenue;
    private BigDecimal yearlyRevenue;
    private Double conversionRate;
    private Double churnRate;
}