package com.emenu.features.hr.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleResponse {
    private UUID id;
    private UUID userId;
    private UserBasicInfo userInfo;
    private UUID businessId;
    private String name;
    private UUID scheduleTypeEnumId;
    private String scheduleTypeEnumName;
    private Set<DayOfWeek> workDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
}