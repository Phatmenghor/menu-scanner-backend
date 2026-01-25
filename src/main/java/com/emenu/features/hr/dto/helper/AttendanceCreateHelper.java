package com.emenu.features.hr.dto.helper;

import com.emenu.enums.hr.AttendanceStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Helper DTO for creating Attendance via MapStruct
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCreateHelper {
    private UUID userId;
    private UUID businessId;
    private UUID workScheduleId;
    private LocalDate attendanceDate;
    private AttendanceStatusEnum status;
}
