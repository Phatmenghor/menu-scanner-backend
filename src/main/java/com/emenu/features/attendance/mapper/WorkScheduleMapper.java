package com.emenu.features.attendance.mapper;

import com.emenu.features.attendance.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.attendance.dto.response.WorkScheduleResponse;
import com.emenu.features.attendance.models.AttendancePolicy;
import com.emenu.features.attendance.models.WorkSchedule;
import com.emenu.features.auth.models.User;
import org.springframework.stereotype.Component;

@Component
public class WorkScheduleMapper {

    public WorkSchedule toEntity(WorkScheduleCreateRequest request, User user, AttendancePolicy policy) {
        return WorkSchedule.builder()
                .user(user)
                .attendancePolicy(policy)
                .scheduleName(request.getScheduleName())
                .scheduleType(request.getScheduleType())
                .workDays(request.getWorkDays())
                .customStartTime(request.getCustomStartTime())
                .customEndTime(request.getCustomEndTime())
                .isActive(request.getIsActive())
                .build();
    }

    public WorkScheduleResponse toResponse(WorkSchedule schedule) {
        return WorkScheduleResponse.builder()
                .id(schedule.getId())
                .userId(schedule.getUser().getId())
                .userName(schedule.getUser().getFirstName() + " " + schedule.getUser().getLastName())
                .policyId(schedule.getAttendancePolicy().getId())
                .policyName(schedule.getAttendancePolicy().getPolicyName())
                .scheduleName(schedule.getScheduleName())
                .scheduleType(schedule.getScheduleType())
                .workDays(schedule.getWorkDays())
                .customStartTime(schedule.getCustomStartTime())
                .customEndTime(schedule.getCustomEndTime())
                .isActive(schedule.getIsActive())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}
