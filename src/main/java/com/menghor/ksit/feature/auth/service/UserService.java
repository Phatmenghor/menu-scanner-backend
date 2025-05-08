package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.request.EnhancedUserUpdateDto;
import com.menghor.ksit.feature.auth.dto.request.StaffUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StudentUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.request.UserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.*;

public interface UserService {
    /**
     * Get all users with advanced filtering (original method)
     */
    UserAllResponseDto getAllUsers(UserFilterRequestDto filterDto);

    /**
     * Get all staff users (admin, teacher, developer, staff)
     */
    StaffUserAllResponseDto getAllStaffUsers(StaffUserFilterRequestDto filterDto);

    /**
     * Get all student users
     */
    StudentUserAllResponseDto getAllStudentUsers(StudentUserFilterRequestDto filterDto);

    /**
     * Get user by ID with detailed information
     */
    UserDetailsResponseDto getUserById(Long id);

    /**
     * Get staff user by ID
     */
    StaffUserResponseDto getStaffUserById(Long id);

    /**
     * Get student user by ID
     */
    StudentUserResponseDto getStudentUserById(Long id);

    /**
     * Get current user details
     */
    UserDetailsResponseDto getCurrentUser();

    /**
     * Update user with enhanced information
     */
    UserDetailsResponseDto updateUser(Long id, EnhancedUserUpdateDto updateDto);

    /**
     * Delete or deactivate user
     */
    UserDetailsResponseDto deleteUser(Long id);

    /**
     * Get dashboard statistics 
     */
    UserStatisticsResponseDto getUserStatistics();
}