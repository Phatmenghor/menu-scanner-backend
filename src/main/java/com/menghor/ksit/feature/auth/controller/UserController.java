package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.constants.SuccessMessages;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordRequestDto;
import com.menghor.ksit.feature.auth.dto.request.UserFilterDto;
import com.menghor.ksit.feature.auth.dto.request.UserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserResponseDto;
import com.menghor.ksit.feature.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping()
    public ApiResponse<UserResponseDto> getAllUsersExcludingShopAdmin (@RequestBody UserFilterDto filterDto) {
        UserResponseDto users = userService.getAllUsers(filterDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.ALL_USERS_FETCHED_SUCCESSFULLY, users);
    }

    /**
     * Get all users, including SHOP_ADMIN
     */
    @PostMapping("all")
    public ApiResponse<UserResponseDto> getAllUsersIncludingShopAdmin(
            @Valid @RequestBody UserFilterDto filterDto
    ) {
        UserResponseDto allUser = userService.getAllUsersIncludingShopAdmin(filterDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.ALL_USERS_FETCHED_SUCCESSFULLY, allUser);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUserDetail(@PathVariable Long id) {
        final UserDto userById = userService.getUserById(id);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_FETCHED_SUCCESSFULLY, userById);
    }

    @GetMapping("/token")
    public ApiResponse<UserDto> getUserByToken() {
        final UserDto userByToken = userService.getUserByToken();
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_FETCHED_SUCCESSFULLY, userByToken);
    }


    @PutMapping("/{id}")
    public ApiResponse<UserDto> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto updateDto) {
        final UserDto updatedUser = userService.updateUser(id, updateDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_UPDATED_SUCCESSFULLY, updatedUser);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<UserDto> deleteUserById(@PathVariable("id") Long userId) {
        final UserDto userDto = userService.deleteUserId(userId);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.USER_FETCHED_SUCCESSFULLY, userDto);
    }

    @PostMapping("change-password")
    public ApiResponse<UserDto> changePassword(@Valid @RequestBody ChangePasswordRequestDto changePasswordDto) {
        final UserDto userDto = userService.changePassword(changePasswordDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.PASSWORD_CHANGED_SUCCESSFULLY, userDto);
    }

    @PostMapping("change-password-by-admin")
    public ApiResponse<UserDto> changePasswordByAdmin(@Valid @RequestBody ChangePasswordByAdminRequestDto changePasswordDto) {
        final UserDto userDto = userService.changePasswordByAdmin(changePasswordDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.PASSWORD_CHANGED_SUCCESSFULLY, userDto);
    }
}