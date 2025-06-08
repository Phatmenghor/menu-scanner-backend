package com.menghor.ksit.feature.survey.dto.update;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SurveyUpdateDto {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Valid
    private List<SurveySectionUpdateDto> sections = new ArrayList<>();
}