package com.emenu.features.attendance.dto.response;

import com.emenu.enums.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long workScheduleId;
    private String scheduleName;
    private LocalDate attendanceDate;

    private LocalDateTime checkInTime;
    private Double checkInLatitude;
    private Double checkInLongitude;
    private String checkInAddress;
    private String checkInNote;

    private LocalDateTime checkOutTime;
    private Double checkOutLatitude;
    private Double checkOutLongitude;
    private String checkOutAddress;
    private String checkOutNote;

    private Integer totalWorkMinutes;
    private Integer lateMinutes;
    private AttendanceStatus status;
    private String remarks;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
