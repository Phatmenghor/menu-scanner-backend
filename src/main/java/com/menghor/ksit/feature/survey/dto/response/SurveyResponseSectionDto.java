package com.menghor.ksit.feature.survey.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SurveyResponseSectionDto {
    private Long sectionId;
    private String title;
    private String description;
    private Integer displayOrder;
    private List<SurveyResponseQuestionDto> questions;
}