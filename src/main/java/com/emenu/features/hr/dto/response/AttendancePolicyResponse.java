package com.emenu.features.hr.dto.response;

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
public class AttendancePolicyResponse {
    private UUID id;
    private UUID businessId;
    private String policyName;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer lateThresholdMinutes;
    private Integer halfDayThresholdMinutes;
    private Integer defaultCheckIns;
}