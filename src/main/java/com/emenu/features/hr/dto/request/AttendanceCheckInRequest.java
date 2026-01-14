package com.emenu.features.hr.dto.request;

import com.emenu.enums.hr.CheckInType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCheckInRequest {
    @NotNull(message = "Work schedule ID required")
    private UUID workScheduleId;
    
    @NotNull(message = "Check-in type required")
    private CheckInType checkInType;
    
    @NotNull(message = "Latitude required")
    private Double latitude;
    
    @NotNull(message = "Longitude required")
    private Double longitude;
    
    private String remarks;
}