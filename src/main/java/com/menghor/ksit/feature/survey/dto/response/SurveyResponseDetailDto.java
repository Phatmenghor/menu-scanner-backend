package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.feature.auth.dto.resposne.UserBasicInfoDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SurveyResponseDetailDto {
    private Long id;
    private Long surveyId;
    private String surveyTitle;
    private UserBasicInfoDto student;
    private ScheduleBasicInfoDto schedule;
    private LocalDateTime submittedAt;
    private Boolean isCompleted;
    private List<SurveyAnswerDetailDto> answerDetails;
    private LocalDateTime createdAt;
}