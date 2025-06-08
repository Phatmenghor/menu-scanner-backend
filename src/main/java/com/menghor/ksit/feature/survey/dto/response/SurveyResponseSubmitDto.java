package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.feature.survey.dto.request.SurveyAnswerSubmitDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SurveyResponseSubmitDto {
    
    @NotNull(message = "Survey ID is required")
    private Long surveyId;
    
    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;
    
    private String overallComment;
    private Double overallRating;
    
    @Valid
    private List<SurveyAnswerSubmitDto> answers;
}