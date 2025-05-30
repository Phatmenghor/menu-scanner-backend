package com.menghor.ksit.feature.attendance.dto.update;

import com.menghor.ksit.enumations.SubmissionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScoreSessionUpdateDto {
    @NotNull(message = "Score session ID is required")
    private Long id;
    private SubmissionStatus status;
    private String teacherComments;
    private String staffComments;
}