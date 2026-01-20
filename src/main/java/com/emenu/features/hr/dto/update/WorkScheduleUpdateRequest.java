package com.emenu.features.hr.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(type = "string", pattern = "HH:mm", example = "09:00", description = "Start time in HH:mm format")
    private LocalTime startTime;

    @Schema(type = "string", pattern = "HH:mm", example = "17:30", description = "End time in HH:mm format")
    private LocalTime endTime;

    @Schema(type = "string", pattern = "HH:mm", example = "12:00", description = "Break start time in HH:mm format")
    private LocalTime breakStartTime;

    @Schema(type = "string", pattern = "HH:mm", example = "13:00", description = "Break end time in HH:mm format")
    private LocalTime breakEndTime;
}