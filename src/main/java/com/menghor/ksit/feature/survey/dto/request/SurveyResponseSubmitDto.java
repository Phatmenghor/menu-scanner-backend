package com.menghor.ksit.feature.survey.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SurveyResponseSubmitDto {
    
    @Valid
    @NotNull(message = "Answers are required")
    private List<SurveyAnswerSubmitDto> answers;
    
    private String overallComment;
    private Double overallRating;
}