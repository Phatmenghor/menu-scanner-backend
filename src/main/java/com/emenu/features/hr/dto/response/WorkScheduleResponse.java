package com.emenu.features.hr.dto.response;

import com.emenu.features.auth.dto.response.UserBasicInfo;
import com.emenu.shared.dto.BaseAuditResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleResponse extends BaseAuditResponse {
    private UserBasicInfo userInfo;
    private UUID businessId;
    private String name;
    private String scheduleTypeEnum;
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