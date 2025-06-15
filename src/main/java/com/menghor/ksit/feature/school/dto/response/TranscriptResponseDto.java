package com.menghor.ksit.feature.school.dto.response;

import com.menghor.ksit.enumations.DegreeEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TranscriptResponseDto {
    private Long studentId;
    private String studentName;
    private String studentCode;
    private String className;
    private String majorName;
    private String departmentName;
    
    // Student personal information
    private LocalDate dateOfBirth;
    private DegreeEnum degree;
    
    // Credit summary
    private Integer numberOfCreditsStudied;
    private Integer numberOfCreditsTransferred;
    private Integer totalNumberOfCreditsEarned;
    private BigDecimal cumulativeGradePointAverage;
    
    // Academic status
    private String academicStatus;

    // All semesters automatically grouped
    private List<TranscriptSemesterDto> semesters;

    private String generatedAt;
}