package com.emenu.features.hr.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceResponse {
    private UUID id;
    private UUID userId;
    private UUID policyId;
    private Integer year;
    private Double totalAllowance;
    private Double usedDays;
    private Double remainingDays;
    private Double carriedForwardDays;
}