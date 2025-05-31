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
}