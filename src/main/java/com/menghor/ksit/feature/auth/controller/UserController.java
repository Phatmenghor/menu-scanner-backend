package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.constants.SuccessMessages;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.request.EnhancedUserUpdateDto;
import com.menghor.ksit.feature.auth.dto.request.UserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserStatisticsResponseDto;
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

    @PostMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<UserAllResponseDto> getAllUsers(@RequestBody UserFilterRequestDto filterDto) {
        log.info("Searching users with filter: {}", filterDto);
        UserAllResponseDto users = userService.getAllUsers(filterDto);
        return new ApiResponse<>("success", "Users retrieved successfully", users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<UserDetailsResponseDto> getUserById(@PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        UserDetailsResponseDto user = userService.getUserById(id);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_FETCHED_SUCCESSFULLY, user);
    }

    @GetMapping("/token")
    public ApiResponse<UserDetailsResponseDto> getCurrentUser() {
        log.info("Fetching current user profile");
        UserDetailsResponseDto user = userService.getCurrentUser();
        return new ApiResponse<>("success", "Current user profile retrieved successfully", user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserDetailsResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody EnhancedUserUpdateDto updateDto) {
        log.info("Updating user with ID: {}", id);
        UserDetailsResponseDto updatedUser = userService.updateUser(id, updateDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_UPDATED_SUCCESSFULLY, updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserDetailsResponseDto> deleteUser(@PathVariable Long id) {
        log.info("Deleting/deactivating user with ID: {}", id);
        UserDetailsResponseDto user = userService.deleteUser(id);
        return new ApiResponse<>("success", "User deactivated successfully", user);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserStatisticsResponseDto> getUserStatistics() {
        log.info("Fetching user statistics");
        UserStatisticsResponseDto statistics = userService.getUserStatistics();
        return new ApiResponse<>("success", "User statistics retrieved successfully", statistics);
    }

}