package com.emenu.features.hr.dto.response;

import com.emenu.enums.hr.CheckInType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCheckInResponse {
    private UUID id;
    private CheckInType checkInType;
    
    private LocalDateTime checkInTime;
    
    private Double latitude;
    private Double longitude;
    private String remarks;
}
