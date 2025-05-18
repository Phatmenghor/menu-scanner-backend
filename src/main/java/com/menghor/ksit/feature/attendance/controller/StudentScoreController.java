package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.StudentScoreUpdateDto;
import com.menghor.ksit.feature.attendance.service.StudentScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student-scores")
@RequiredArgsConstructor
@Slf4j
public class StudentScoreController {
    
    private final StudentScoreService studentScoreService;
    
    @GetMapping("/{id}")
    public ApiResponse<StudentScoreResponseDto> getStudentScoreById(@PathVariable Long id) {
        log.info("REST request to get student score by ID: {}", id);
        StudentScoreResponseDto responseDto = studentScoreService.getStudentScoreById(id);
        return new ApiResponse<>(
                "success",
                "Student score retrieved successfully",
                responseDto
        );
    }
    
    @PutMapping("/update")
    public ApiResponse<StudentScoreResponseDto> updateStudentScore(
            @Valid @RequestBody StudentScoreUpdateDto updateDto) {
        log.info("REST request to update student score: {}", updateDto);
        StudentScoreResponseDto responseDto = studentScoreService.updateStudentScore(updateDto);
        return new ApiResponse<>(
                "success",
                "Student score updated successfully",
                responseDto
        );
    }
    
    @GetMapping("/student/{studentId}")
    public ApiResponse<List<StudentScoreResponseDto>> getScoresByStudentId(@PathVariable Long studentId) {
        log.info("REST request to get scores for student ID: {}", studentId);
        List<StudentScoreResponseDto> responseDtos = studentScoreService.getScoresByStudentId(studentId);
        return new ApiResponse<>(
                "success",
                "Student scores retrieved successfully",
                responseDtos
        );
    }
    
    @PostMapping("/create/{studentId}/{scoreSessionId}")
    public ApiResponse<StudentScoreResponseDto> createStudentScore(
            @PathVariable Long studentId, @PathVariable Long scoreSessionId) {
        log.info("REST request to create score for student ID: {} in session ID: {}", 
                studentId, scoreSessionId);
        StudentScoreResponseDto responseDto = studentScoreService.createStudentScore(studentId, scoreSessionId);
        return new ApiResponse<>(
                "success",
                "Student score created successfully",
                responseDto
        );
    }
}