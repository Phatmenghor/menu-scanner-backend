package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.feature.auth.dto.resposne.UserBasicInfoDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudentSurveyResponseDto {
    private Long id;
    private Long surveyId;
    private String surveyTitle;
    private String surveyDescription;
    private UserBasicInfoDto user;
    private LocalDateTime submittedAt;
    private Boolean isCompleted;
    
    // Full survey structure as it was when submitted
    private List<SurveyResponseSectionDto> sections;
    
    private LocalDateTime createdAt;
}
