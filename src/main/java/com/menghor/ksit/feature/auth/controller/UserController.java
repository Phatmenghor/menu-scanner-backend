package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.constants.SuccessMessages;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordRequestDto;
import com.menghor.ksit.feature.auth.dto.request.UserFilterDto;
import com.menghor.ksit.feature.auth.dto.request.UserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsDto;
import com.menghor.ksit.feature.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping()
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<UserAllResponseDto> getAllUsers(@RequestBody UserFilterDto filterDto) {
        log.info("Fetching all users with filter: {}", filterDto);
        UserAllResponseDto users = userService.getAllUsers(filterDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.ALL_USERS_FETCHED_SUCCESSFULLY, users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<UserDetailsDto> getUserDetail(@PathVariable Long id) {
        log.info("Fetching user by ID: {}", id);
        UserDetailsDto userById = userService.getUserById(id);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_FETCHED_SUCCESSFULLY, userById);
    }

    @GetMapping("/token")
    public ApiResponse<UserDetailsDto> getUserByToken() {
        log.info("Fetching user from token");
        UserDetailsDto userByToken = userService.getUserByToken();
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_FETCHED_SUCCESSFULLY, userByToken);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserDetailsDto> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto updateDto) {
        log.info("Updating user with ID: {}", id);
        UserDetailsDto updatedUser = userService.updateUser(id, updateDto);
        log.info("User updated with ID: {}", id);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_UPDATED_SUCCESSFULLY, updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserDetailsDto> deleteUserById(@PathVariable("id") Long userId) {
        log.warn("Deleting user with ID: {}", userId);
        UserDetailsDto userDto = userService.deleteUserId(userId);
        return new ApiResponse<>(SuccessMessages.SUCCESS, "User deleted successfully", userDto);
    }

    @PostMapping("/change-password")
    public ApiResponse<UserDetailsDto> changePassword(@Valid @RequestBody ChangePasswordRequestDto changePasswordDto) {
        log.info("Changing password for current user");
        UserDetailsDto userDto = userService.changePassword(changePasswordDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.PASSWORD_CHANGED_SUCCESSFULLY, userDto);
    }

    @PostMapping("/change-password-by-admin")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserDetailsDto> changePasswordByAdmin(@Valid @RequestBody ChangePasswordByAdminRequestDto changePasswordDto) {
        log.warn("Admin changing password for user ID: {}", changePasswordDto.getId());
        UserDetailsDto userDto = userService.changePasswordByAdmin(changePasswordDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.PASSWORD_CHANGED_SUCCESSFULLY, userDto);
    }
}
