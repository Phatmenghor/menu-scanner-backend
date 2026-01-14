package com.emenu.features.leave.dto.response;

import com.emenu.enums.leave.LeaveSession;
import com.emenu.enums.leave.LeaveStatus;
import com.emenu.enums.leave.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long leavePolicyId;
    private String policyName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveSession session;
    private Double totalDays;
    private String reason;
    private LeaveStatus status;
    private Long approvedBy;
    private String approverName;
    private ZonedDateTime approvedAt;
    private String approverNote;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
