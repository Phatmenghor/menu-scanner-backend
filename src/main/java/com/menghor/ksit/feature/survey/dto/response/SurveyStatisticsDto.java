package com.menghor.ksit.feature.survey.dto.response;

import lombok.Data;

@Data
public class SurveyStatisticsDto {
    private Long scheduleId;
    private String scheduleName;
    private Integer totalStudents;
    private Integer completedResponses;
    private Integer pendingResponses;
    private Double completionRate;
    private Double averageRating;
    private Integer totalQuestions;
}