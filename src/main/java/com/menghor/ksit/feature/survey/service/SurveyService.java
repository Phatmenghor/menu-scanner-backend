package com.menghor.ksit.feature.survey.service;

import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.*;
import com.menghor.ksit.feature.survey.dto.update.SurveyUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface SurveyService {

    // Get the main survey (admin view with all details)
    SurveyResponseDto getMainSurvey();

    // Update the main survey content (admin only)
    SurveyResponseDto updateMainSurvey(SurveyUpdateDto updateDto);

    // NEW METHODS - Delete and return updated survey
    SurveyResponseDto deleteSurveySectionAndGetUpdatedSurvey(Long sectionId);
    SurveyResponseDto deleteSurveyQuestionAndGetUpdatedSurvey(Long questionId);

    // Submit response to the survey for a specific schedule (students)
    StudentSurveyResponseDto submitSurveyResponseForSchedule(Long scheduleId, SurveyResponseSubmitDto submitDto);

    // Get current user's response for a specific schedule (student)
    StudentSurveyResponseDto getMyResponseForSchedule(Long scheduleId);

    // Get detailed survey response (admin/staff)
    SurveyResponseDetailDto getStudentResponseDetail(Long responseId);

    // Check if user has completed survey for schedule
    Boolean hasUserCompletedSurvey(Long userId, Long scheduleId);
}
