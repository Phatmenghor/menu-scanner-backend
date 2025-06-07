package com.menghor.ksit.feature.attendance.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
public class ScoreConfigurationRequestDto {

    @NotNull(message = "Attendance percentage is required")
    @Min(value = 0, message = "Attendance percentage must be at least 0")
    @Max(value = 100, message = "Attendance percentage cannot exceed 100")
    private Integer attendancePercentage;

    @NotNull(message = "Assignment percentage is required")
    @Min(value = 0, message = "Assignment percentage must be at least 0")
    @Max(value = 100, message = "Assignment percentage cannot exceed 100")
    private Integer assignmentPercentage;

    @NotNull(message = "Midterm percentage is required")
    @Min(value = 0, message = "Midterm percentage must be at least 0")
    @Max(value = 100, message = "Midterm percentage cannot exceed 100")
    private Integer midtermPercentage;

    @NotNull(message = "Final percentage is required")
    @Min(value = 0, message = "Final percentage must be at least 0")
    @Max(value = 100, message = "Final percentage cannot exceed 100")
    private Integer finalPercentage;

    // Computed field for validation
    public Integer getTotalPercentage() {
        if (attendancePercentage == null || assignmentPercentage == null ||
                midtermPercentage == null || finalPercentage == null) {
            return 0;
        }
        return attendancePercentage + assignmentPercentage + midtermPercentage + finalPercentage;
    }
}