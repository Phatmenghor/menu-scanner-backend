package com.emenu.features.leave.mapper;

import com.emenu.features.auth.models.Business;
import com.emenu.features.leave.dto.request.LeavePolicyCreateRequest;
import com.emenu.features.leave.dto.response.LeavePolicyResponse;
import com.emenu.features.leave.models.LeavePolicy;
import org.springframework.stereotype.Component;

@Component
public class LeavePolicyMapper {

    public LeavePolicy toEntity(LeavePolicyCreateRequest request, Business business) {
        return LeavePolicy.builder()
                .business(business)
                .policyName(request.getPolicyName())
                .leaveType(request.getLeaveType())
                .annualAllowance(request.getAnnualAllowance())
                .allowHalfDay(request.getAllowHalfDay())
                .requiresApproval(request.getRequiresApproval())
                .minAdvanceNoticeDays(request.getMinAdvanceNoticeDays())
                .maxConsecutiveDays(request.getMaxConsecutiveDays())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .build();
    }

    public LeavePolicyResponse toResponse(LeavePolicy policy) {
        return LeavePolicyResponse.builder()
                .id(policy.getId())
                .businessId(policy.getBusiness().getId())
                .businessName(policy.getBusiness().getBusinessName())
                .policyName(policy.getPolicyName())
                .leaveType(policy.getLeaveType())
                .annualAllowance(policy.getAnnualAllowance())
                .allowHalfDay(policy.getAllowHalfDay())
                .requiresApproval(policy.getRequiresApproval())
                .minAdvanceNoticeDays(policy.getMinAdvanceNoticeDays())
                .maxConsecutiveDays(policy.getMaxConsecutiveDays())
                .description(policy.getDescription())
                .isActive(policy.getIsActive())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
