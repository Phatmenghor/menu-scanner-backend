package com.menghor.ksit.feature.survey.service;

import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.StudentSurveyResponseDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyResponseDto;
import com.menghor.ksit.feature.survey.dto.update.SurveyUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface SurveyService {
    
    // Get the single active survey
    SurveyResponseDto getActiveSurvey();
    
    // Update the survey content (admin only)
    SurveyResponseDto updateSurvey(SurveyUpdateDto updateDto);
    
    // Submit response to the survey (students)
    StudentSurveyResponseDto submitSurveyResponse(SurveyResponseSubmitDto submitDto);
    
    // Get all responses to the survey (admin/staff)
    CustomPaginationResponseDto<StudentSurveyResponseDto> getAllResponses(int pageNo, int pageSize);
    
    // Get current user's response (student)
    StudentSurveyResponseDto getMyResponse();
    
    // Initialize default survey if not exists
    void initializeDefaultSurvey();
}