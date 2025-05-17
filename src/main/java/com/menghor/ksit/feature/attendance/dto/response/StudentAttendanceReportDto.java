package com.menghor.ksit.feature.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceReportDto {
    private Long studentId;
    private String studentName;
    private String studentCode;
    private String className;
    private String academicYear;
    private String semester;
    private List<CourseAttendanceDto> courses;
    private Double overallAttendancePercentage;
}