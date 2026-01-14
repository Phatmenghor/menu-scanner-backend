package com.emenu.features.hr.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePolicyCreateRequest {
    @NotNull(message = "Business ID required")
    private UUID businessId;
    
    @NotBlank(message = "Policy name required")
    private String policyName;
    
    private String description;
    
    @NotNull(message = "Start time required")
    private LocalTime startTime;
    
    @NotNull(message = "End time required")
    private LocalTime endTime;
    
    private Integer lateThresholdMinutes;
    private Integer halfDayThresholdMinutes;
    
    @NotNull(message = "Default check-ins required")
    @Min(value = 2, message = "Minimum 2 check-ins")
    @Max(value = 4, message = "Maximum 4 check-ins")
    private Integer defaultCheckIns = 2;
}
