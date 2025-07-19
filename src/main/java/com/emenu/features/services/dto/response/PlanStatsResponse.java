package com.emenu.features.services.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanStatsResponse {
    private String planName;
    private Long subscriberCount;
    private BigDecimal revenue;
    private Double percentage;
}