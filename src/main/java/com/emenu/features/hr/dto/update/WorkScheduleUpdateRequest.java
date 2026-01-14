package com.emenu.features.hr.dto.update;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    private String name;
    private String scheduleTypeEnumName;
    private Set<DayOfWeek> workDays;
    private LocalTime startTime;
    private LocalTime endTime;
    
    @Min(value = 2, message = "Minimum 2 check-ins required")
    @Max(value = 4, message = "Maximum 4 check-ins allowed")
    private Integer requiredCheckIns;
    
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
}