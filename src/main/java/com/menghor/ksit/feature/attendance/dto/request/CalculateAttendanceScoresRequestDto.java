package com.menghor.ksit.feature.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CalculateAttendanceScoresRequestDto {
    @NotNull(message = "Score session ID is required")
    private Long scoreSessionId;
}