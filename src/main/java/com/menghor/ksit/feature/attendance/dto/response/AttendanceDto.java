package com.menghor.ksit.feature.attendance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.menghor.ksit.enumations.AttendanceFinalizationStatus;
import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.enumations.AttendanceType;
import com.menghor.ksit.enumations.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    // Teacher information
    private Long teacherId;
    private String teacherName;

    // Student personal information
    private GenderEnum gender;
    private LocalDate dateOfBirth;

    // Schedule and Course information
    private Long scheduleId;
    private String courseName;
}