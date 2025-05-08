package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.request.EnhancedUserUpdateDto;
import com.menghor.ksit.feature.auth.dto.request.UserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserStatisticsResponseDto;

public interface UserService {
    /**
     * Get all users with advanced filtering
     */
    UserAllResponseDto getAllUsers(UserFilterRequestDto filterDto);
    
    /**
     * Get user by ID with detailed information
     */
    UserDetailsResponseDto getUserById(Long id);
    
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