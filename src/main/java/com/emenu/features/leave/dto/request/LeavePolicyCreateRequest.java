package com.emenu.features.leave.dto.request;

import com.emenu.enums.leave.LeaveType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicyCreateRequest {

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotBlank(message = "Policy name is required")
    private String policyName;

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotNull(message = "Annual allowance is required")
    @Min(value = 0, message = "Annual allowance must be at least 0")
    private Double annualAllowance;

    @NotNull(message = "Allow half day flag is required")
    private Boolean allowHalfDay;

    @NotNull(message = "Requires approval flag is required")
    private Boolean requiresApproval;

    @NotNull(message = "Minimum advance notice days is required")
    @Min(value = 0, message = "Minimum advance notice days must be at least 0")
    private Integer minAdvanceNoticeDays;

    @NotNull(message = "Maximum consecutive days is required")
    @Min(value = 0, message = "Maximum consecutive days must be at least 0")
    private Double maxConsecutiveDays;

    private String description;

    @Builder.Default
    private Boolean isActive = true;
}
