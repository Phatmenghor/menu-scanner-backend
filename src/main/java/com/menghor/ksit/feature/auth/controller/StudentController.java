package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.request.StudentUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsDto;
import com.menghor.ksit.feature.auth.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;

    /**
     * Get detailed student information by ID
     * Accessible by ADMIN, DEVELOPER, and STAFF roles
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<UserDetailsDto> getStudentById(@PathVariable Long id) {
        log.info("Fetching student details for ID: {}", id);
        UserDetailsDto student = studentService.getStudentDetails(id);
        log.info("Student details retrieved successfully for ID: {}", id);
        return new ApiResponse<>("success", "Student details retrieved successfully", student);
    }

    /**
     * Update student information
     * Accessible by ADMIN and DEVELOPER roles
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserDetailsDto> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateDto updateDto) {
        log.info("Updating student with ID: {}", id);
        UserDetailsDto updatedStudent = studentService.updateStudent(id, updateDto);
        log.info("Student updated successfully with ID: {}", id);
        return new ApiResponse<>("success", "Student updated successfully", updatedStudent);
    }

    /**
     * Get current student's own details
     * Only accessible to STUDENT role
     */
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ApiResponse<UserDetailsDto> getCurrentStudentDetails() {
        log.info("Fetching current student details");
        UserDetailsDto student = studentService.getCurrentStudentDetails();
        log.info("Current student details retrieved successfully");
        return new ApiResponse<>("success", "Student details retrieved successfully", student);
    }
}