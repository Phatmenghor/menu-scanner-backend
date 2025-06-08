package com.menghor.ksit.feature.survey.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SurveyCreateRequestDto {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;
    
    @Valid
    private List<SurveySectionCreateDto> sections = new ArrayList<>();
}
