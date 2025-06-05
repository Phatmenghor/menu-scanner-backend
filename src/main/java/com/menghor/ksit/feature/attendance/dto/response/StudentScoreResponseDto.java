package com.menghor.ksit.feature.attendance.dto.response;

import com.menghor.ksit.enumations.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentScoreResponseDto {
    private Long id;
    private String studentNameKhmer;
    private String studentNameEnglish;
    private Long studentIdentityNumber;
    private GenderEnum gender;
    private Long studentId;
    private LocalDate dateOfBirth;

    // Raw scores (0-100 each) - what teachers enter
    private Double attendanceRawScore;    // Raw attendance score out of 100
    private Double assignmentRawScore;    // Raw assignment score out of 100
    private Double midtermRawScore;       // Raw midterm score out of 100
    private Double finalRawScore;         // Raw final score out of 100

    // Weighted scores (calculated based on configuration) - what gets used for final grade
    private Double attendanceScore;       // Weighted attendance score (raw * percentage)
    private Double assignmentScore;       // Weighted assignment score (raw * percentage)
    private Double midtermScore;          // Weighted midterm score (raw * percentage)
    private Double finalScore;            // Weighted final score (raw * percentage)

    private Double totalScore;            // Sum of all weighted scores (max 100)
    private String grade;                 // Letter grade (A+, A, B+, etc.)
    private String comments;              // Teacher comments
    private String createdAt;
}