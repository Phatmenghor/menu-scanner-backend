package com.emenu.features.attendance.dto.update;

import com.emenu.enums.attendance.WorkScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleUpdateRequest {

    private String scheduleName;
    private WorkScheduleType scheduleType;
    private Set<DayOfWeek> workDays;
    private LocalTime customStartTime;
    private LocalTime customEndTime;
    private Boolean isActive;
}
