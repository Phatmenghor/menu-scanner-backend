package com.menghor.ksit.feature.survey.dto.update;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SurveyUpdateDto {
    private String title;
    private String description;

    @Valid
    private List<SurveySectionUpdateDto> sections = new ArrayList<>();
}