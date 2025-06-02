package com.menghor.ksit.feature.setting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponseDto {
    private long totalRooms;
    private long totalStudents;
    private long totalTeachers;
    private long totalCourses;
    private long totalClasses;
    private long totalMajors;
    private long totalDepartments;
}