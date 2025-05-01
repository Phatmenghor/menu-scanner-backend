package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.constants.SuccessMessages;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordRequestDto;
import com.menghor.ksit.feature.auth.dto.request.UserFilterDto;
import com.menghor.ksit.feature.auth.dto.request.UserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserResponseDto;
import com.menghor.ksit.feature.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Get all users with pagination and filtering
     * Only accessible by ADMIN and DEVELOPER roles
     */
    @PostMapping()
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserResponseDto> getAllUsers(@RequestBody UserFilterDto filterDto) {
        UserResponseDto users = userService.getAllUsers(filterDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.ALL_USERS_FETCHED_SUCCESSFULLY, users);
    }

    /**
     * Get a specific user by ID
     * ADMIN and DEVELOPER can access any user
     * STAFF can only access STUDENT users and themselves
     * STUDENT can only access themselves
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER') or " +
            "(hasAuthority('STAFF') and @userPermissionEvaluator.canAccessUser(authentication, #id)) or " +
            "(hasAuthority('STUDENT') and @userPermissionEvaluator.isSameUser(authentication, #id))")
    public ApiResponse<UserDto> getUserDetail(@PathVariable Long id) {
        final UserDto userById = userService.getUserById(id);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_FETCHED_SUCCESSFULLY, userById);
    }

    /**
     * Get current user details from token
     * Accessible by all authenticated users
     */
    @GetMapping("/token")
    public ApiResponse<UserDto> getUserByToken() {
        final UserDto userByToken = userService.getUserByToken();
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_FETCHED_SUCCESSFULLY, userByToken);
    }

    /**
     * Update user information
     * ADMIN and DEVELOPER can update any user
     * STAFF can only update STUDENT users and limited self-information
     * STUDENT can only update limited self-information
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER') or " +
            "(hasAuthority('STAFF') and @userPermissionEvaluator.canModifyUser(authentication, #id)) or " +
            "(hasAuthority('STUDENT') and @userPermissionEvaluator.canStudentUpdateSelf(authentication, #id))")
    public ApiResponse<UserDto> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto updateDto) {
        final UserDto updatedUser = userService.updateUser(id, updateDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_UPDATED_SUCCESSFULLY, updatedUser);
    }

    /**
     * Delete a user
     * Only accessible by ADMIN and DEVELOPER roles
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserDto> deleteUserById(@PathVariable("id") Long userId) {
        final UserDto userDto = userService.deleteUserId(userId);
        return new ApiResponse<>(SuccessMessages.SUCCESS, "User deleted successfully", userDto);
    }

    /**
     * Change own password
     * Accessible by all authenticated users for their own password
     */
    @PostMapping("/change-password")
    public ApiResponse<UserDto> changePassword(@Valid @RequestBody ChangePasswordRequestDto changePasswordDto) {
        final UserDto userDto = userService.changePassword(changePasswordDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.PASSWORD_CHANGED_SUCCESSFULLY, userDto);
    }

    /**
     * Change another user's password (admin action)
     * Only accessible by ADMIN and DEVELOPER roles
     */
    @PostMapping("/change-password-by-admin")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserDto> changePasswordByAdmin(@Valid @RequestBody ChangePasswordByAdminRequestDto changePasswordDto) {
        final UserDto userDto = userService.changePasswordByAdmin(changePasswordDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.PASSWORD_CHANGED_SUCCESSFULLY, userDto);
    }

    /**
     * Get all student users
     * Accessible by ADMIN, DEVELOPER, and STAFF roles
     */
    @PostMapping("/students")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<UserResponseDto> getAllStudents(@RequestBody UserFilterDto filterDto) {
        // Set role filter to STUDENT
        filterDto.setRole(RoleEnum.STUDENT);
        UserResponseDto students = userService.getAllUsers(filterDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, "All students fetched successfully", students);
    }

    /**
     * Get all staff users
     * Accessible by ADMIN and DEVELOPER roles
     */
    @PostMapping("/staff")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserResponseDto> getAllStaff(@RequestBody UserFilterDto filterDto) {
        // Set role filter to STAFF
        filterDto.setRole(RoleEnum.STAFF);
        UserResponseDto staff = userService.getAllUsers(filterDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, "All staff members fetched successfully", staff);
    }
}