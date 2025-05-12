package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.request.StudentBatchCreateRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StudentCreateRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StudentUpdateRequestDto;
import com.menghor.ksit.feature.auth.dto.filter.StudentUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserResponseDto;

import java.util.List;

/**
 * Service interface for student user management
 */
public interface StudentService {

    /**
     * Register a new student with detailed information
     */
    StudentUserResponseDto registerStudent(StudentCreateRequestDto registerDto);

    /**
     * Get all student users with advanced filtering
     */
    StudentUserAllResponseDto getAllStudentUsers(StudentUserFilterRequestDto filterDto);

    /**
     * Get student user by ID with detailed information
     */
    StudentUserResponseDto getStudentUserById(Long id);


    List<StudentResponseDto> batchRegisterStudents(StudentBatchCreateRequestDto batchRequest);

    /**
     * Update student user with detailed information
     */
    StudentUserResponseDto updateStudentUser(Long id, StudentUpdateRequestDto updateDto);

    /**
     * Delete or deactivate student user
     */
    StudentUserResponseDto deleteStudentUser(Long id);
}