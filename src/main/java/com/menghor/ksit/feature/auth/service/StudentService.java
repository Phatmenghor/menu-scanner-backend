package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.request.StudentUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentDetailsDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDto;

public interface StudentService {
    
    /**
     * Get detailed student information by ID
     */
    StudentDetailsDto getStudentDetails(Long id);
    
    /**
     * Get current logged-in student's details
     */
    StudentDetailsDto getCurrentStudentDetails();
    
    /**
     * Update student information
     */
    UserDto updateStudent(Long id, StudentUpdateDto updateDto);
}