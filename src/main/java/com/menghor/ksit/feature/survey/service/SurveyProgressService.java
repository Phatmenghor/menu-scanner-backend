package com.menghor.ksit.feature.survey.service;

import com.menghor.ksit.feature.survey.dto.response.ScheduleStudentsProgressDto;

public interface SurveyProgressService {
    
    /**
     * Get all students in a schedule's class with their survey completion status
     * Shows full student information like StudentUserListResponseDto plus survey status
     */
    ScheduleStudentsProgressDto getScheduleStudentsProgress(Long scheduleId);
}