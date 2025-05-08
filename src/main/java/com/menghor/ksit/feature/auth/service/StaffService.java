package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.request.StaffCreateRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StaffUpdateRequestDto;
import com.menghor.ksit.feature.auth.dto.filter.StaffUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserResponseDto;

/**
 * Service interface for staff user management
 */
public interface StaffService {

    /**
     * Register a new staff user with detailed information
     */
    StaffUserResponseDto registerStaff(StaffCreateRequestDto registerDto);

    /**
     * Get all staff users with advanced filtering
     */
    StaffUserAllResponseDto getAllStaffUsers(StaffUserFilterRequestDto filterDto);

    /**
     * Get staff user by ID with detailed information
     */
    StaffUserResponseDto getStaffUserById(Long id);

    /**
     * Update staff user with detailed information
     */
    StaffUserResponseDto updateStaffUser(Long id, StaffUpdateRequestDto updateDto);

    /**
     * Delete or deactivate staff user
     */
    StaffUserResponseDto deleteStaffUser(Long id);
}