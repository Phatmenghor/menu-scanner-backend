package com.emenu.features.hr.dto.update;

import jakarta.validation.constraints.Max;
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
    private Integer lateThresholdMinutes;
    private Integer halfDayThresholdMinutes;
    
    @Min(value = 2, message = "Minimum 2 check-ins")
    @Max(value = 4, message = "Maximum 4 check-ins")
    private Integer defaultCheckIns;
}