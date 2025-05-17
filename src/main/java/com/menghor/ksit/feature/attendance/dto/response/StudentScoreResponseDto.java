package com.menghor.ksit.feature.attendance.dto.response;

import lombok.Data;

@Data
public class StudentScoreResponseDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private Double attendanceScore;
    private Double assignmentScore;
    private Double midtermScore;
    private Double finalScore;
    private Double totalScore;
    private String grade;
    private String comments;
}