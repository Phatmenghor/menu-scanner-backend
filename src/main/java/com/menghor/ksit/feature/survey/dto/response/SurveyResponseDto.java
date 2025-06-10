package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.dto.resposne.UserBasicInfoDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SurveyResponseDto {
    private Long id;
    private String title;
    private String description;
    private Status status;
    private UserBasicInfoDto createdBy;
    private List<SurveySectionResponseDto> sections;
    private LocalDateTime createdAt;
}