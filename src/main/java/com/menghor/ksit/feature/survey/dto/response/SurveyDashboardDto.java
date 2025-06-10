package com.menghor.ksit.feature.survey.dto.response;

import lombok.Data;

@Data
public class SurveyDashboardDto {
    private String surveyTitle;
    private Integer totalResponses;
    private Integer totalStudents;
    private Integer totalSchedules;
    private Integer pendingResponses;
    private Double overallCompletionRate;
    private Double averageRating;
    private String lastUpdated;
}