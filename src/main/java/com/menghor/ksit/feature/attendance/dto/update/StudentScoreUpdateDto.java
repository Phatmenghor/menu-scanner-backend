package com.menghor.ksit.feature.attendance.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

@Data
public class StudentScoreUpdateDto {
    @NotNull(message = "Student score ID is required")
    private Long id;

    @DecimalMin(value = "0.0", message = "Assignment score must be at least 0")
    private Double assignmentScore;

    @DecimalMin(value = "0.0", message = "Midterm score must be at least 0")
    private Double midtermScore;

    @DecimalMin(value = "0.0", message = "Final score must be at least 0")
    private Double finalScore;

    private String comments;
}