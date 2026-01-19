package com.emenu.features.hr.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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
public class WorkScheduleCreateRequest {
    @NotNull(message = "User ID required")
    private UUID userId;

    @NotNull(message = "Business ID required")
    private UUID businessId;

    @NotBlank(message = "Name required")
    private String name;

    private String scheduleTypeEnumName;

    @NotEmpty(message = "Work days required")
    private Set<DayOfWeek> workDays;

    @NotNull(message = "Start time required")
    @Schema(type = "string", pattern = "HH:mm", example = "09:00", description = "Start time in HH:mm format")
    private LocalTime startTime;

    @NotNull(message = "End time required")
    @Schema(type = "string", pattern = "HH:mm", example = "17:30", description = "End time in HH:mm format")
    private LocalTime endTime;

    @Schema(type = "string", pattern = "HH:mm", example = "12:00", description = "Break start time in HH:mm format")
    private LocalTime breakStartTime;

    @Schema(type = "string", pattern = "HH:mm", example = "13:00", description = "Break end time in HH:mm format")
    private LocalTime breakEndTime;
}
