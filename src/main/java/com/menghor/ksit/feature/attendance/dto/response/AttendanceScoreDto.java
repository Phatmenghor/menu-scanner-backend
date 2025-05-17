package com.menghor.ksit.feature.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceScoreDto {
    private Long studentId;
    private String studentName;
    private String studentCode;
    private Long scheduleId;
    private String courseName;
    private String className;
    private Long totalSessions;
    private Long attendedSessions;
    private Double attendancePercentage;
}