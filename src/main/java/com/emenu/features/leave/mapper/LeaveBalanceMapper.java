package com.emenu.features.leave.mapper;

import com.emenu.features.leave.dto.response.LeaveBalanceResponse;
import com.emenu.features.leave.models.LeaveBalance;
import org.springframework.stereotype.Component;

@Component
public class LeaveBalanceMapper {

    public LeaveBalanceResponse toResponse(LeaveBalance balance) {
        return LeaveBalanceResponse.builder()
                .id(balance.getId())
                .userId(balance.getUser().getId())
                .userName(balance.getUser().getFirstName() + " " + balance.getUser().getLastName())
                .leavePolicyId(balance.getLeavePolicy().getId())
                .policyName(balance.getLeavePolicy().getPolicyName())
                .leaveType(balance.getLeavePolicy().getLeaveType())
                .year(balance.getYear())
                .totalAllowance(balance.getTotalAllowance())
                .usedDays(balance.getUsedDays())
                .remainingDays(balance.getRemainingDays())
                .createdAt(balance.getCreatedAt())
                .updatedAt(balance.getUpdatedAt())
                .build();
    }
}
