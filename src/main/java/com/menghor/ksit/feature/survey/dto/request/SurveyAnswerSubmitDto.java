package com.menghor.ksit.feature.survey.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SurveyAnswerSubmitDto {
    @NotNull(message = "Question ID is required")
    private Long questionId;
    
    private String textAnswer;
    private Integer ratingAnswer;
}
