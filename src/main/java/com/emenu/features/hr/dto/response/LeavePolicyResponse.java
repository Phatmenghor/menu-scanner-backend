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
public class LeavePolicyResponse {
    private UUID id;
    private UUID businessId;
    private UUID typeEnumId;
    private String typeEnumName;
    private String policyName;
    private String description;
    private Double annualAllowance;
    private Boolean allowHalfDay;
    private Boolean requiresApproval;
    private Integer minAdvanceNoticeDays;
    private Double maxConsecutiveDays;
}
