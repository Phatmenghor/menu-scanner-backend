package com.menghor.ksit.feature.survey.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SurveySectionSnapshotDto {
    private Long id;
    private String title;
    private String description;
    private Integer displayOrder;
    private List<SurveyQuestionSnapshotDto> questions;
}