package com.menghor.ksit.feature.attendance.service;

import com.menghor.ksit.feature.attendance.dto.request.BatchUpdateScoresRequestDto;
import com.menghor.ksit.feature.attendance.dto.request.CalculateAttendanceScoresRequestDto;
import com.menghor.ksit.feature.attendance.dto.request.ScoreSessionRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.ScoreSessionUpdateDto;

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
     * Get a score session by schedule ID
     */
    ScoreSessionResponseDto getScoreSessionByScheduleId(Long scheduleId);
    
    /**
     * Update score session status and comments
     */
    ScoreSessionResponseDto updateScoreSession(ScoreSessionUpdateDto updateDto);
    
    /**
     * Submit score session for review
     */
    ScoreSessionResponseDto submitForReview(Long scoreSessionId, String comments);
    
    /**
     * Staff review of score session (approve/reject/pending)
     */
    ScoreSessionResponseDto reviewScoreSession(Long scoreSessionId, String status, String comments);

    /**
     * Get all score sessions for staff review
     */
    List<ScoreSessionResponseDto> getScoreSessionsForReview();

}