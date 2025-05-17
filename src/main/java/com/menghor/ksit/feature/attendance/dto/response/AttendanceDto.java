package com.menghor.ksit.feature.attendance.dto.response;

import com.menghor.ksit.enumations.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {
    private Long id;
    private AttendanceStatus status;
    private String comment;
    private LocalDateTime recordedTime;
    private boolean isFinal;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private Long attendanceSessionId;
}