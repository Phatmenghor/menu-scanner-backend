package com.menghor.ksit.feature.attendance.service;

import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.StudentScoreUpdateDto;

import java.util.List;

public interface StudentScoreService {
    
    /**
     * Get a student score by its ID
     */
    StudentScoreResponseDto getStudentScoreById(Long id);
    
    /**
     * Update a student score
     */
    StudentScoreResponseDto updateStudentScore(StudentScoreUpdateDto updateDto);
    
    /**
     * Get all scores for a student
     */
    List<StudentScoreResponseDto> getScoresByStudentId(Long studentId);
    
    /**
     * Calculate attendance score for a student in a session
     */
    void calculateAttendanceScore(Long studentId, Long scoreSessionId);
    
    /**
     * Calculate total score and grade for a student score
     */
    void calculateTotalScore(Long studentScoreId);
    
    /**
     * Create a new student score entry for a student in a score session
     */
    StudentScoreResponseDto createStudentScore(Long studentId, Long scoreSessionId);
}