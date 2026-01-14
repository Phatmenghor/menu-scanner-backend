package com.emenu.features.attendance.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePolicyCreateRequest {

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotBlank(message = "Policy name is required")
    private String policyName;

    private String description;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Late threshold minutes is required")
    @Min(value = 0, message = "Late threshold must be at least 0")
    private Integer lateThresholdMinutes;

    @NotNull(message = "Half day threshold minutes is required")
    @Min(value = 0, message = "Half day threshold must be at least 0")
    private Integer halfDayThresholdMinutes;

    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    @NotNull(message = "Require location check flag is required")
    private Boolean requireLocationCheck;

    private Double officeLatitude;
    private Double officeLongitude;

    @Min(value = 0, message = "Allowed radius must be at least 0")
    private Integer allowedRadiusMeters;

    @Builder.Default
    private Boolean isActive = true;
}
