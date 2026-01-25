package com.emenu.features.hr.dto.helper;

import com.emenu.enums.hr.CheckInType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Helper DTO for creating AttendanceCheckIn via MapStruct
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCheckInCreateHelper {
    private CheckInType checkInType;
    private LocalDateTime checkInTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String remarks;
}
