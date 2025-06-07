package com.menghor.ksit.feature.attendance.dto.response;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentScoreResponseDto {
    private Long id;
    private String studentNameKhmer;
    private String studentNameEnglish;
    private Long studentIdentityNumber;
    private String gender;
    private Long studentId;
    private LocalDate dateOfBirth;

    // Raw scores
    private BigDecimal attendanceRawScore;
    private BigDecimal assignmentRawScore;
    private BigDecimal midtermRawScore;
    private BigDecimal finalRawScore;

    // Weighted scores
    private BigDecimal attendanceScore;
    private BigDecimal assignmentScore;
    private BigDecimal midtermScore;
    private BigDecimal finalScore;

    private BigDecimal totalScore;
    private String grade;
    private String comments;
    private LocalDateTime createdAt;
}