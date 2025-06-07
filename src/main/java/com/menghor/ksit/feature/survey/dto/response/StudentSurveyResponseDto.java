package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.feature.auth.dto.resposne.UserBasicInfoDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudentSurveyResponseDto {
    private Long id;
    private Long surveyId;
    private UserBasicInfoDto user;
    private LocalDateTime submittedAt;
    private Boolean isCompleted;
    private List<StudentSurveyAnswerDto> answers;
    private LocalDateTime createdAt;
}