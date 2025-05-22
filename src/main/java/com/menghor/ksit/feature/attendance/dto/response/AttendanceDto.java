package com.menghor.ksit.feature.attendance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.menghor.ksit.enumations.AttendanceFinalizationStatus;
import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.enumations.AttendanceType;
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
    private AttendanceType attendanceType;
    private String identifyNumber;
    private String comment;
    private LocalDateTime recordedTime;
    private AttendanceFinalizationStatus finalizationStatus;
    private Long studentId;
    private String studentName;
    private Long attendanceSessionId;
    private String createdAt;
}