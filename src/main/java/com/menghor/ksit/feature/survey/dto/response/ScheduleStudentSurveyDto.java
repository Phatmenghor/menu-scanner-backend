package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.SurveyStatus;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ScheduleStudentSurveyDto {
    // Basic user info (same as StudentUserListResponseDto)
    private Long id;
    private String username;
    private String email;
    private Status status;

    // Personal info (same as StudentUserListResponseDto)
    private String khmerFirstName;
    private String khmerLastName;
    private String englishFirstName;
    private String englishLastName;
    private String profileUrl;
    private GenderEnum gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String identifyNumber;
    private ClassResponseDto studentClass;
    private LocalDateTime createdAt;

    // Survey status info
    private SurveyStatus surveyStatus; // NOT_STARTED, COMPLETED
    private LocalDateTime surveySubmittedAt; // null if not completed
    private Long surveyResponseId; // null if not completed
}