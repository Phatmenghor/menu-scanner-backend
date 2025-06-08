package com.menghor.ksit.feature.school.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TranscriptResponseDto {
    private Long studentId;
    private String studentName;
    private String studentCode;
    private String className;
    private String majorName;
    private String departmentName;

    // Overall statistics
    private Integer totalCreditsEarned;
    private Integer totalCreditsAttempted;
    private BigDecimal overallGPA;
    private String academicStatus;

    // All semesters automatically grouped
    private List<TranscriptSemesterDto> semesters;

    private String generatedAt;
}