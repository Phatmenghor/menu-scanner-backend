package com.menghor.ksit.feature.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScoreSessionRequestDto {
    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;
}