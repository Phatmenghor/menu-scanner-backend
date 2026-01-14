package com.emenu.features.hr.dto.request;

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
    
    @NotNull(message = "Policy ID required")
    private UUID policyId;
    
    @NotBlank(message = "Name required")
    private String name;
    
    private String scheduleTypeEnumName;
    
    @NotEmpty(message = "Work days required")
    private Set<DayOfWeek> workDays;
    
    @NotNull(message = "Start time required")
    private LocalTime startTime;
    
    @NotNull(message = "End time required")
    private LocalTime endTime;
    
    @NotNull(message = "Required check-ins must be specified")
    @Min(value = 2, message = "Minimum 2 check-ins required")
    @Max(value = 4, message = "Maximum 4 check-ins allowed")
    private Integer requiredCheckIns = 2;
    
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
}
