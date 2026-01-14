package com.emenu.features.attendance.dto.response;

import com.emenu.enums.attendance.WorkScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long policyId;
    private String policyName;
    private String scheduleName;
    private WorkScheduleType scheduleType;
    private Set<DayOfWeek> workDays;
    private LocalTime customStartTime;
    private LocalTime customEndTime;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
