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
public class LeaveBalanceResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long leavePolicyId;
    private String policyName;
    private LeaveType leaveType;
    private Integer year;
    private Double totalAllowance;
    private Double usedDays;
    private Double remainingDays;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
