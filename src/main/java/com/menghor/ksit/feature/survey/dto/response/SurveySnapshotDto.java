package com.menghor.ksit.feature.survey.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SurveySnapshotDto {
    private Long id;
    private String title;
    private String description;
    private List<SurveySectionSnapshotDto> sections;
}