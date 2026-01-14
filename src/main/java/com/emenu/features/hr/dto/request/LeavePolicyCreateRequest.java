package com.emenu.features.hr.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicyCreateRequest {
    @NotNull(message = "Business ID required")
    private UUID businessId;
    
    @NotBlank(message = "Type enum name required")
    private String typeEnumName;
    
    @NotBlank(message = "Policy name required")
    private String policyName;
    
    private String description;
    private Double annualAllowance;
    private Boolean allowHalfDay;
    private Boolean requiresApproval;
    private Integer minAdvanceNoticeDays;
    private Double maxConsecutiveDays;
}