package com.menghor.ksit.feature.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentScoreResponseDto {
    private Long id;
    private String studentId;
    private String studentNameKhmer;
    private String studentNameEnglish;
    private Double attendanceScore; // Out of 10 points
    private Double assignmentScore; // Out of 20 points
    private Double midtermScore;    // Out of 30 points
    private Double finalScore;      // Out of 40 points
    private Double totalScore;      // Out of 100 points
    private String grade;
    private String comments;
    private String createdAt;
}