package com.emenu.features.hr.dto.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicyUpdateRequest {
    private String policyName;
    private String description;
    private String typeEnumName;
    private Double annualAllowance;
    private Boolean allowHalfDay;
    private Boolean requiresApproval;
    private Integer minAdvanceNoticeDays;
    private Double maxConsecutiveDays;
}