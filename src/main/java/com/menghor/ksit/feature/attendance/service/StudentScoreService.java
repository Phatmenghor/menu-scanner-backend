package com.menghor.ksit.feature.attendance.service;

import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.StudentScoreUpdateDto;

import java.util.List;

public interface StudentScoreService {
    StudentScoreResponseDto getStudentScoreById(Long id);
    StudentScoreResponseDto updateStudentScore(StudentScoreUpdateDto updateDto);
}