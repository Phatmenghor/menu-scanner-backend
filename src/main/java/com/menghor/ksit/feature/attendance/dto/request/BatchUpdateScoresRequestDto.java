package com.menghor.ksit.feature.attendance.dto.request;

import com.menghor.ksit.feature.attendance.dto.update.StudentScoreUpdateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchUpdateScoresRequestDto {
    @NotNull(message = "Score session ID is required")
    private Long scoreSessionId;
    
    @NotEmpty(message = "At least one student score must be provided")
    @Valid
    private List<StudentScoreUpdateDto> studentScores;
}