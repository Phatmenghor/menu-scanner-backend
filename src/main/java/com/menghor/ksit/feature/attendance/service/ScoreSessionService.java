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
     * Update multiple student scores in a batch
     */
    ScoreSessionResponseDto batchUpdateStudentScores(BatchUpdateScoresRequestDto requestDto);
    
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
     * Calculate attendance scores for all students in a session
     */
    void calculateAttendanceScores(CalculateAttendanceScoresRequestDto requestDto);
    
    /**
     * Get all score sessions for a teacher
     */
    List<ScoreSessionResponseDto> getScoreSessionsByTeacherId(Long teacherId);
    
    /**
     * Get all score sessions for staff review
     */
    List<ScoreSessionResponseDto> getScoreSessionsForReview();
    
    /**
     * Process newly added student to class
     * This ensures the student has score records in all score sessions for that class
     */
    void processNewlyAddedStudent(Long studentId, Long classId);
}