package com.emenu.features.attendance.dto.request;

import com.emenu.enums.attendance.WorkScheduleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class WorkScheduleCreateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotBlank(message = "Schedule name is required")
    private String scheduleName;

    @NotNull(message = "Schedule type is required")
    private WorkScheduleType scheduleType;

    @NotEmpty(message = "Work days are required")
    private Set<DayOfWeek> workDays;

    private LocalTime customStartTime;
    private LocalTime customEndTime;

    @Builder.Default
    private Boolean isActive = true;
}
