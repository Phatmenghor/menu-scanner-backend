package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.constants.SuccessMessages;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.request.StaffCreateRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StaffUpdateRequestDto;
import com.menghor.ksit.feature.auth.dto.filter.StaffUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserResponseDto;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.service.StaffService;
import com.menghor.ksit.utils.database.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
@Slf4j
public class StaffController {

    private final StaffService staffService;
    private final SecurityUtils securityUtils;

    /**
     * Register a new staff member
     */
    @PostMapping("/register")
    public ApiResponse<StaffUserResponseDto> registerStaff(@Valid @RequestBody StaffCreateRequestDto requestDto) {
        log.info("Registering staff user with email: {}", requestDto.getEmail());
        StaffUserResponseDto registeredUser = staffService.registerStaff(requestDto);
        log.info("Staff user registered successfully with ID: {}", registeredUser.getId());
        return new ApiResponse<>("success", "Staff registered successfully", registeredUser);
    }

    /**
     * Get all staff users with filtering
     */
    @PostMapping("/all")
    public ApiResponse<StaffUserAllResponseDto> getAllStaffUsers(@RequestBody StaffUserFilterRequestDto filterDto) {
        log.info("Searching staff users with filter: {}", filterDto);
        StaffUserAllResponseDto users = staffService.getAllStaffUsers(filterDto);
        return new ApiResponse<>("success", "Staff users retrieved successfully", users);
    }

    /**
     * Get staff user by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<StaffUserResponseDto> getStaffUserById(@PathVariable Long id) {
        log.info("Fetching staff user with ID: {}", id);
        StaffUserResponseDto user = staffService.getStaffUserById(id);
        return new ApiResponse<>("success", "Staff user fetched successfully", user);
    }

    /**
     * Update staff user
     */
    @PutMapping("/{id}")
    public ApiResponse<StaffUserResponseDto> updateStaffUser(@PathVariable Long id, @Valid @RequestBody StaffUpdateRequestDto updateDto) {
        log.info("Updating staff user with ID: {}", id);
        StaffUserResponseDto updatedUser = staffService.updateStaffUser(id, updateDto);
        return new ApiResponse<>("success", "Staff user updated successfully", updatedUser);
    }

    /**
     * Update staff user token
     */
    @PutMapping("token")
    public ApiResponse<StaffUserResponseDto> updateStaffByTokenUser(@Valid @RequestBody StaffUpdateRequestDto updateDto) {
        final UserEntity currentEntity = securityUtils.getCurrentUser();
        log.info("Updating staff token user with ID: {}", currentEntity.getId());
        StaffUserResponseDto updatedUser = staffService.updateStaffUser(currentEntity.getId(), updateDto);
        return new ApiResponse<>("success", "Staff user updated successfully", updatedUser);
    }

    /**
     * Delete/deactivate staff user
     */
    @DeleteMapping("/{id}")
    public ApiResponse<StaffUserResponseDto> deleteStaffUser(@PathVariable Long id) {
        log.info("Deleting/deactivating staff user with ID: {}", id);
        StaffUserResponseDto user = staffService.deleteStaffUser(id);
        return new ApiResponse<>("success", "Staff user deactivated successfully", user);
    }
}