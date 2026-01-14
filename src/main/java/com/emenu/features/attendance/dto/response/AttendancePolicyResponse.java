package com.emenu.features.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePolicyResponse {

    private Long id;
    private Long businessId;
    private String businessName;
    private String policyName;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer lateThresholdMinutes;
    private Integer halfDayThresholdMinutes;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private Boolean requireLocationCheck;
    private Double officeLatitude;
    private Double officeLongitude;
    private Integer allowedRadiusMeters;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
