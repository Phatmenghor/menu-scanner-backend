package com.menghor.ksit.feature.school.dto.response;

import com.menghor.ksit.enumations.SemesterEnum;
import com.menghor.ksit.enumations.YearLevelEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TranscriptSemesterDto {
    private Integer academyYear;
    private SemesterEnum semester;
    private String semesterName; // e.g., "Semester 1, 2024"
    private YearLevelEnum yearLevel; // Year 1, 2, 3, 4

    // Semester totals
    private Integer totalCredits;
    private BigDecimal gpa;
    private BigDecimal gpax; // Cumulative GPA up to this semester

    // Courses in this semester
    private List<TranscriptCourseDto> courses;
}