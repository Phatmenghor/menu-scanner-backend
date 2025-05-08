package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.constants.SuccessMessages;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.request.EnhancedUserUpdateDto;
import com.menghor.ksit.feature.auth.dto.request.StaffUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StudentUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.request.UserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.*;
import com.menghor.ksit.feature.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Original endpoint - kept for backward compatibility
     */
    @PostMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<UserAllResponseDto> getAllUsers(@RequestBody UserFilterRequestDto filterDto) {
        log.info("Searching users with filter: {}", filterDto);
        UserAllResponseDto users = userService.getAllUsers(filterDto);
        return new ApiResponse<>("success", "Users retrieved successfully", users);
    }

    /**
     * Get all staff users (admin, teacher, developer, staff)
     */
    @PostMapping("/staff")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<StaffUserAllResponseDto> getAllStaffUsers(@RequestBody StaffUserFilterRequestDto filterDto) {
        log.info("Searching staff users with filter: {}", filterDto);
        StaffUserAllResponseDto users = userService.getAllStaffUsers(filterDto);
        return new ApiResponse<>("success", "Staff users retrieved successfully", users);
    }

    /**
     * Get all student users
     */
    @PostMapping("/students")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<StudentUserAllResponseDto> getAllStudentUsers(@RequestBody StudentUserFilterRequestDto filterDto) {
        log.info("Searching student users with filter: {}", filterDto);
        StudentUserAllResponseDto users = userService.getAllStudentUsers(filterDto);
        return new ApiResponse<>("success", "Student users retrieved successfully", users);
    }

    /**
     * Original endpoint - get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<UserDetailsResponseDto> getUserById(@PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        UserDetailsResponseDto user = userService.getUserById(id);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_FETCHED_SUCCESSFULLY, user);
    }

    /**
     * Get staff user by ID
     */
    @GetMapping("/staff/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<StaffUserResponseDto> getStaffUserById(@PathVariable Long id) {
        log.info("Fetching staff user with ID: {}", id);
        StaffUserResponseDto user = userService.getStaffUserById(id);
        return new ApiResponse<>(SuccessMessages.SUCCESS, "Staff user fetched successfully", user);
    }

    /**
     * Get student user by ID
     */
    @GetMapping("/student/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<StudentUserResponseDto> getStudentUserById(@PathVariable Long id) {
        log.info("Fetching student user with ID: {}", id);
        StudentUserResponseDto user = userService.getStudentUserById(id);
        return new ApiResponse<>(SuccessMessages.SUCCESS, "Student user fetched successfully", user);
    }

    /**
     * Get current user profile
     */
    @GetMapping("/token")
    public ApiResponse<UserDetailsResponseDto> getCurrentUser() {
        log.info("Fetching current user profile");
        UserDetailsResponseDto user = userService.getCurrentUser();
        return new ApiResponse<>("success", "Current user profile retrieved successfully", user);
    }

    /**
     * Update user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserDetailsResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody EnhancedUserUpdateDto updateDto) {
        log.info("Updating user with ID: {}", id);
        UserDetailsResponseDto updatedUser = userService.updateUser(id, updateDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_UPDATED_SUCCESSFULLY, updatedUser);
    }

    /**
     * Delete/deactivate user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserDetailsResponseDto> deleteUser(@PathVariable Long id) {
        log.info("Deleting/deactivating user with ID: {}", id);
        UserDetailsResponseDto user = userService.deleteUser(id);
        return new ApiResponse<>("success", "User deactivated successfully", user);
    }

    /**
     * Get user statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserStatisticsResponseDto> getUserStatistics() {
        log.info("Fetching user statistics");
        UserStatisticsResponseDto statistics = userService.getUserStatistics();
        return new ApiResponse<>("success", "User statistics retrieved successfully", statistics);
    }
}