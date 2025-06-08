package com.menghor.ksit.feature.survey.dto.filter;

import lombok.Data;

@Data
public class SurveyFilterDto {
    private Long scheduleId;
    private Long studentId;
    private Boolean completedOnly;
    private String searchTerm; // for student name or schedule name
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}