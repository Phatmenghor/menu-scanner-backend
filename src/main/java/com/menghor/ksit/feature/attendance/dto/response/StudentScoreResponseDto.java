package com.menghor.ksit.feature.attendance.dto.response;

import com.menghor.ksit.enumations.GenderEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentScoreResponseDto {
    private Long id;
    private String studentNameKhmer;
    private String studentNameEnglish;
    private String studentIdentityNumber;
    private GenderEnum gender;
    private Long studentId;
    private LocalDate dateOfBirth;

    // Final scores only
    private BigDecimal attendanceScore;
    private BigDecimal assignmentScore;
    private BigDecimal midtermScore;
    private BigDecimal finalScore;

    private BigDecimal totalScore;
    private String grade;
    private String comments;
    private LocalDateTime createdAt;
}