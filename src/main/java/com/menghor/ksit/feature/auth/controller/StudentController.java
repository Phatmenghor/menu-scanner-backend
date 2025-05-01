package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.request.StudentUpdateDto;
import com.menghor.ksit.feature.auth.dto.request.UserFilterDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentDetailsDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserResponseDto;
import com.menghor.ksit.feature.auth.service.StudentService;
import com.menghor.ksit.feature.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final UserService userService;

    /**
     * Get all students with pagination and filters
     * Accessible by ADMIN, DEVELOPER, and STAFF roles
     */
    @PostMapping("/list")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<UserResponseDto> getAllStudents(@RequestBody UserFilterDto filterDto) {
        // Set role filter to STUDENT
        filterDto.setRole(RoleEnum.STUDENT);
        UserResponseDto students = userService.getAllUsers(filterDto);
        return new ApiResponse<>("success", "Students retrieved successfully", students);
    }

    /**
     * Get detailed student information by ID
     * Accessible by ADMIN, DEVELOPER, STAFF, and the student themselves
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF') or " +
            "(hasAuthority('STUDENT') and @userPermissionEvaluator.isSameUser(authentication, #id))")
    public ApiResponse<StudentDetailsDto> getStudentById(@PathVariable Long id) {
        StudentDetailsDto student = studentService.getStudentDetails(id);
        return new ApiResponse<>("success", "Student details retrieved successfully", student);
    }

    /**
     * Update student information
     * ADMIN and DEVELOPER can update any student
     * STAFF can update limited student information
     * STUDENT can update their own limited information
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER') or " +
            "(hasAuthority('STAFF') and @userPermissionEvaluator.canModifyUser(authentication, #id)) or " +
            "(hasAuthority('STUDENT') and @userPermissionEvaluator.canStudentUpdateSelf(authentication, #id))")
    public ApiResponse<UserDto> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateDto updateDto) {
        UserDto updatedStudent = studentService.updateStudent(id, updateDto);
        return new ApiResponse<>("success", "Student updated successfully", updatedStudent);
    }

    /**
     * Get current student's own details
     * Only accessible to STUDENT role, returns their own details
     */
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ApiResponse<StudentDetailsDto> getCurrentStudentDetails() {
        StudentDetailsDto student = studentService.getCurrentStudentDetails();
        return new ApiResponse<>("success", "Student details retrieved successfully", student);
    }
}