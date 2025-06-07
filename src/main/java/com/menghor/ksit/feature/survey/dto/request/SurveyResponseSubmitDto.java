package com.menghor.ksit.feature.survey.dto.request;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class SurveyResponseSubmitDto {
    @Valid
    private List<SurveyAnswerSubmitDto> answers;
}