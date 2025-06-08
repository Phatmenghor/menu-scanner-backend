package com.menghor.ksit.feature.survey.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SurveySectionCreateDto {
    
    @NotBlank(message = "Section title is required")
    private String title;
    
    private String description;
    private Integer displayOrder = 0;
    
    @Valid
    private List<SurveyQuestionCreateDto> questions = new ArrayList<>();
}