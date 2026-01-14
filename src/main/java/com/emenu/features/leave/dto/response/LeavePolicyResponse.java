package com.emenu.features.leave.dto.response;

import com.emenu.enums.leave.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicyResponse {

    private Long id;
    private Long businessId;
    private String businessName;
    private String policyName;
    private LeaveType leaveType;
    private Double annualAllowance;
    private Boolean allowHalfDay;
    private Boolean requiresApproval;
    private Integer minAdvanceNoticeDays;
    private Double maxConsecutiveDays;
    private String description;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
