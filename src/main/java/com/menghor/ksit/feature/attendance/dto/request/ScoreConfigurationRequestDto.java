package com.menghor.ksit.feature.attendance.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreConfigurationRequestDto {

    @NotNull(message = "Attendance percentage is required")
    @DecimalMin(value = "0.00", message = "Attendance percentage must be at least 0")
    @DecimalMax(value = "100.00", message = "Attendance percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Attendance percentage must have at most 3 digits and 2 decimal places")
    private BigDecimal attendancePercentage;

    @NotNull(message = "Assignment percentage is required")
    @DecimalMin(value = "0.00", message = "Assignment percentage must be at least 0")
    @DecimalMax(value = "100.00", message = "Assignment percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Assignment percentage must have at most 3 digits and 2 decimal places")
    private BigDecimal assignmentPercentage;

    @NotNull(message = "Midterm percentage is required")
    @DecimalMin(value = "0.00", message = "Midterm percentage must be at least 0")
    @DecimalMax(value = "100.00", message = "Midterm percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Midterm percentage must have at most 3 digits and 2 decimal places")
    private BigDecimal midtermPercentage;

    @NotNull(message = "Final percentage is required")
    @DecimalMin(value = "0.00", message = "Final percentage must be at least 0")
    @DecimalMax(value = "100.00", message = "Final percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Final percentage must have at most 3 digits and 2 decimal places")
    private BigDecimal finalPercentage;

    public BigDecimal getTotalPercentage() {
        if (attendancePercentage == null || assignmentPercentage == null ||
                midtermPercentage == null || finalPercentage == null) {
            return BigDecimal.ZERO;
        }
        return attendancePercentage.add(assignmentPercentage)
                .add(midtermPercentage).add(finalPercentage);
    }
}