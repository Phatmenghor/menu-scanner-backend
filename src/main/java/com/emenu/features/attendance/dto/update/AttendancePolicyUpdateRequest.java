package com.emenu.features.attendance.dto.update;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePolicyUpdateRequest {

    private String policyName;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;

    @Min(value = 0, message = "Late threshold must be at least 0")
    private Integer lateThresholdMinutes;

    @Min(value = 0, message = "Half day threshold must be at least 0")
    private Integer halfDayThresholdMinutes;

    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private Boolean requireLocationCheck;
    private Double officeLatitude;
    private Double officeLongitude;

    @Min(value = 0, message = "Allowed radius must be at least 0")
    private Integer allowedRadiusMeters;

    private Boolean isActive;
}
