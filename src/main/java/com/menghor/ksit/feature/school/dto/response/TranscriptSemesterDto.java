package com.menghor.ksit.feature.school.dto.response;

import com.menghor.ksit.enumations.SemesterEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TranscriptSemesterDto {
    private Integer academyYear;
    private SemesterEnum semester;
    private String semesterName; // e.g., "Semester 1, 2024"

    // Semester statistics
    private Integer semesterCreditsEarned;
    private Integer semesterCreditsAttempted;
    private BigDecimal semesterGPA;

    // Courses in this semester
    private List<TranscriptCourseDto> courses;

    // Running totals up to this semester
    private Integer cumulativeCreditsEarned;
    private Integer cumulativeCreditsAttempted;
    private BigDecimal cumulativeGPA;
}