package com.menghor.ksit.feature.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CourseAttendanceDto {
    private Long courseId;
    private String courseName;
    private Long scheduleId;
    private Long totalSessions;
    private Long attendedSessions;
    private Double attendancePercentage;
}