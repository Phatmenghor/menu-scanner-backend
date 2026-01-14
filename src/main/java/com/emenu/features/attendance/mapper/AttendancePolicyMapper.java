package com.emenu.features.attendance.mapper;

import com.emenu.features.attendance.dto.request.AttendancePolicyCreateRequest;
import com.emenu.features.attendance.dto.response.AttendancePolicyResponse;
import com.emenu.features.attendance.models.AttendancePolicy;
import com.emenu.features.auth.models.Business;
import org.springframework.stereotype.Component;

@Component
public class AttendancePolicyMapper {

    public AttendancePolicy toEntity(AttendancePolicyCreateRequest request, Business business) {
        return AttendancePolicy.builder()
                .business(business)
                .policyName(request.getPolicyName())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .lateThresholdMinutes(request.getLateThresholdMinutes())
                .halfDayThresholdMinutes(request.getHalfDayThresholdMinutes())
                .breakStartTime(request.getBreakStartTime())
                .breakEndTime(request.getBreakEndTime())
                .requireLocationCheck(request.getRequireLocationCheck())
                .officeLatitude(request.getOfficeLatitude())
                .officeLongitude(request.getOfficeLongitude())
                .allowedRadiusMeters(request.getAllowedRadiusMeters())
                .isActive(request.getIsActive())
                .build();
    }

    public AttendancePolicyResponse toResponse(AttendancePolicy policy) {
        return AttendancePolicyResponse.builder()
                .id(policy.getId())
                .businessId(policy.getBusiness().getId())
                .businessName(policy.getBusiness().getBusinessName())
                .policyName(policy.getPolicyName())
                .description(policy.getDescription())
                .startTime(policy.getStartTime())
                .endTime(policy.getEndTime())
                .lateThresholdMinutes(policy.getLateThresholdMinutes())
                .halfDayThresholdMinutes(policy.getHalfDayThresholdMinutes())
                .breakStartTime(policy.getBreakStartTime())
                .breakEndTime(policy.getBreakEndTime())
                .requireLocationCheck(policy.getRequireLocationCheck())
                .officeLatitude(policy.getOfficeLatitude())
                .officeLongitude(policy.getOfficeLongitude())
                .allowedRadiusMeters(policy.getAllowedRadiusMeters())
                .isActive(policy.getIsActive())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
