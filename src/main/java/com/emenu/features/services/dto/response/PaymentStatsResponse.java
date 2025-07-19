package com.emenu.features.services.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentStatsResponse {
    private Long totalPayments;
    private Long successfulPayments;
    private Long failedPayments;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private Double successRate;
}