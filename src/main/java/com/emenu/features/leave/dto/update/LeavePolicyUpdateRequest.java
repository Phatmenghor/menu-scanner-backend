package com.emenu.features.leave.dto.update;

import com.emenu.enums.leave.LeaveType;
import jakarta.validation.constraints.Min;
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
    private LeaveType leaveType;

    @Min(value = 0, message = "Annual allowance must be at least 0")
    private Double annualAllowance;

    private Boolean allowHalfDay;
    private Boolean requiresApproval;

    @Min(value = 0, message = "Minimum advance notice days must be at least 0")
    private Integer minAdvanceNoticeDays;

    @Min(value = 0, message = "Maximum consecutive days must be at least 0")
    private Double maxConsecutiveDays;

    private String description;
    private Boolean isActive;
}
