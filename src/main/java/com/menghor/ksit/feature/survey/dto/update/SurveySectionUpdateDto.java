package com.menghor.ksit.feature.survey.dto.update;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SurveySectionUpdateDto {
    private Long id; // For updating existing sections

    @NotBlank(message = "Section title is required")
    private String title;

    private String description;
    private Integer displayOrder = 0;

    @Valid
    private List<SurveyQuestionUpdateDto> questions = new ArrayList<>();
}