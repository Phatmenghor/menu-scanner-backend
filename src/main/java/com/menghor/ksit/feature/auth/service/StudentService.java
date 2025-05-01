package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.request.StudentUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsDto;

public interface StudentService {

    /**
     * Get detailed student information by ID
     */
    UserDetailsDto getStudentDetails(Long id);

    /**
     * Get current logged-in student's details
     */
    UserDetailsDto getCurrentStudentDetails();

    /**
     * Update student information
     */
    UserDetailsDto updateStudent(Long id, StudentUpdateDto updateDto);
}