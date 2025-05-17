package com.menghor.ksit.feature.attendance.dto.response;

import com.menghor.ksit.enumations.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionDto {
    private Long id;
    private LocalDateTime sessionDate;
    private String qrCode;
    private LocalDateTime qrExpiryTime;
    private boolean isFinal;
    private Status status;
    private Long scheduleId;
    private String courseName;
    private String roomName;
    private String className;
    private Long teacherId;
    private String teacherName;
    private List<AttendanceDto> attendances;
}