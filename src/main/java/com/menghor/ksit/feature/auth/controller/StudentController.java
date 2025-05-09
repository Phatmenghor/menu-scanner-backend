package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.constants.SuccessMessages;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.request.StudentCreateRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StudentUpdateRequestDto;
import com.menghor.ksit.feature.auth.dto.filter.StudentUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserResponseDto;
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
     * Register a new student
     */
    @PostMapping("/register")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<StudentUserResponseDto> registerStudent(@Valid @RequestBody StudentCreateRequestDto requestDto) {
        log.info("Registering student with email: {}", requestDto.getEmail());
        StudentUserResponseDto registeredStudent = studentService.registerStudent(requestDto);
        log.info("Student registered successfully with ID: {}", registeredStudent.getId());
        return new ApiResponse<>("success", "Student registered successfully", registeredStudent);
    }

    /**
     * Get all student users with filtering
     */
    @PostMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<StudentUserAllResponseDto> getAllStudentUsers(@RequestBody StudentUserFilterRequestDto filterDto) {
        log.info("Searching student users with filter: {}", filterDto);
        StudentUserAllResponseDto users = studentService.getAllStudentUsers(filterDto);
        return new ApiResponse<>("success", "Student users retrieved successfully", users);
    }

    /**
     * Get student user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<StudentUserResponseDto> getStudentUserById(@PathVariable Long id) {
        log.info("Fetching student user with ID: {}", id);
        StudentUserResponseDto user = studentService.getStudentUserById(id);
        return new ApiResponse<>(SuccessMessages.SUCCESS, "Student user fetched successfully", user);
    }

    /**
     * Update student user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<StudentUserResponseDto> updateStudentUser(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequestDto updateDto) {
        log.info("Updating student user with ID: {}", id);
        StudentUserResponseDto updatedUser = studentService.updateStudentUser(id, updateDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, "Student user updated successfully", updatedUser);
    }

    /**
     * Delete/deactivate student user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<StudentUserResponseDto> deleteStudentUser(@PathVariable Long id) {
        log.info("Deleting/deactivating student user with ID: {}", id);
        StudentUserResponseDto user = studentService.deleteStudentUser(id);
        return new ApiResponse<>("success", "Student user deactivated successfully", user);
    }
}