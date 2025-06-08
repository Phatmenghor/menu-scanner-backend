package com.menghor.ksit.feature.survey.service;

import com.menghor.ksit.enumations.SurveyStatus;
import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.*;
import com.menghor.ksit.feature.survey.dto.update.SurveyUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface SurveyService {

    // Get the single active survey for a specific schedule
    SurveyResponseDto getActiveSurveyForSchedule(Long scheduleId);

    // Update the survey content (admin only)
    SurveyResponseDto updateSurvey(SurveyUpdateDto updateDto);

    // Submit response to the survey for a specific schedule (students)
    StudentSurveyResponseDto submitSurveyResponseForSchedule(Long scheduleId, SurveyResponseSubmitDto submitDto);

    // Get all responses to the survey (admin/staff) - can filter by schedule
    CustomPaginationResponseDto<StudentSurveyResponseDto> getAllResponses(int pageNo, int pageSize, Long scheduleId);

    // Get current user's response for a specific schedule (student)
    StudentSurveyResponseDto getMyResponseForSchedule(Long scheduleId);

    // Get all schedules for current student with survey status
    CustomPaginationResponseDto<StudentScheduleWithSurveyDto> getMySchedulesWithSurveyStatus(int pageNo, int pageSize);

    // Get specific student response detail (admin/staff)
    SurveyResponseDetailDto getStudentResponseDetail(Long responseId);

    // Get all survey responses for a specific schedule (admin/staff)
    CustomPaginationResponseDto<StudentSurveyResponseDto> getScheduleSurveyResponses(Long scheduleId, int pageNo, int pageSize);

    // Initialize default survey if not exists
    void initializeDefaultSurvey();

    // Get survey status for a schedule (for current student)
    SurveyStatus getSurveyStatusForSchedule(Long scheduleId);

    // Get survey statistics for admin
    SurveyStatisticsDto getSurveyStatistics(Long scheduleId);
}