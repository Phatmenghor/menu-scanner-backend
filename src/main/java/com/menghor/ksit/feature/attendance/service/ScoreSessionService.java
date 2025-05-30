package com.menghor.ksit.feature.attendance.service;

import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.feature.attendance.dto.filter.ScoreSessionFilterDto;
import com.menghor.ksit.feature.attendance.dto.request.BatchUpdateScoresRequestDto;
import com.menghor.ksit.feature.attendance.dto.request.CalculateAttendanceScoresRequestDto;
import com.menghor.ksit.feature.attendance.dto.request.ScoreSessionRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.ScoreSessionUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

import java.util.List;

public interface ScoreSessionService {
    
    /**
     * Initialize or get existing score session for a schedule
     */
    ScoreSessionResponseDto initializeScoreSession(ScoreSessionRequestDto requestDto);
    
    /**
     * Get a score session by its ID
     */
    ScoreSessionResponseDto getScoreSessionById(Long id);

    /**
     * Update score session status and comments
     */
    ScoreSessionResponseDto updateScoreSession(ScoreSessionUpdateDto updateDto);

    /**
     * Get all score sessions with pagination and filtering
     */
    CustomPaginationResponseDto<ScoreSessionResponseDto> getAllScoreSessions(ScoreSessionFilterDto filterDto);

}