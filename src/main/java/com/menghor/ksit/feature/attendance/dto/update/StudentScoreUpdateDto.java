package com.menghor.ksit.feature.attendance.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentScoreUpdateDto {
    @NotNull(message = "Student score ID is required")
    private Long id;
    
    private Double assignmentScore;
    private Double midtermScore;
    private Double finalScore;
    private String comments;
}