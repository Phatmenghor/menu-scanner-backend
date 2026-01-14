package com.emenu.features.leave.mapper;

import com.emenu.enums.leave.LeaveStatus;
import com.emenu.features.auth.models.User;
import com.emenu.features.leave.dto.request.LeaveCreateRequest;
import com.emenu.features.leave.dto.response.LeaveResponse;
import com.emenu.features.leave.models.Leave;
import com.emenu.features.leave.models.LeavePolicy;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {

    public Leave toEntity(LeaveCreateRequest request, User user, LeavePolicy policy) {
        double totalDays = calculateTotalDays(request.getStartDate(), request.getEndDate(), request.getSession());

        return Leave.builder()
                .user(user)
                .leavePolicy(policy)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .session(request.getSession())
                .totalDays(totalDays)
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();
    }

    public LeaveResponse toResponse(Leave leave) {
        return LeaveResponse.builder()
                .id(leave.getId())
                .userId(leave.getUser().getId())
                .userName(leave.getUser().getFirstName() + " " + leave.getUser().getLastName())
                .leavePolicyId(leave.getLeavePolicy().getId())
                .policyName(leave.getLeavePolicy().getPolicyName())
                .leaveType(leave.getLeavePolicy().getLeaveType())
                .startDate(leave.getStartDate())
                .endDate(leave.getEndDate())
                .session(leave.getSession())
                .totalDays(leave.getTotalDays())
                .reason(leave.getReason())
                .status(leave.getStatus())
                .approvedBy(leave.getApprovedBy() != null ? leave.getApprovedBy().getId() : null)
                .approverName(leave.getApprovedBy() != null ?
                        leave.getApprovedBy().getFirstName() + " " + leave.getApprovedBy().getLastName() : null)
                .approvedAt(leave.getApprovedAt())
                .approverNote(leave.getApproverNote())
                .createdAt(leave.getCreatedAt())
                .updatedAt(leave.getUpdatedAt())
                .build();
    }

    private double calculateTotalDays(java.time.LocalDate startDate, java.time.LocalDate endDate,
                                     com.emenu.enums.leave.LeaveSession session) {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return daysBetween * session.getDays();
    }
}
