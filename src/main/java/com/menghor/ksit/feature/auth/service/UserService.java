package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordRequestDto;
import com.menghor.ksit.feature.auth.dto.request.UserFilterDto;
import com.menghor.ksit.feature.auth.dto.request.UserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserResponseDto;

public interface UserService {
    UserResponseDto getAllUsers(UserFilterDto filterDto);
    UserResponseDto getAllUsersIncludingShopAdmin(UserFilterDto filterDto);
    UserDto getUserById(Long id);
    UserDto getUserByToken();
    UserDto updateUser(Long id, UserUpdateDto updateDto);
    UserDto deleteUserId(Long id);
    UserDto changePassword(ChangePasswordRequestDto requestDto);
    UserDto changePasswordByAdmin(ChangePasswordByAdminRequestDto requestDto);
}