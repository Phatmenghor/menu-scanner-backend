package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordRequestDto;
import com.menghor.ksit.feature.auth.dto.request.UserFilterDto;
import com.menghor.ksit.feature.auth.dto.request.UserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsDto;

public interface UserService {
    UserAllResponseDto getAllUsers(UserFilterDto filterDto);
    UserAllResponseDto getAllUsersIncludingShopAdmin(UserFilterDto filterDto);
    UserDetailsDto getUserById(Long id);
    UserDetailsDto getUserByToken();
    UserDetailsDto updateUser(Long id, UserUpdateDto updateDto);
    UserDetailsDto deleteUserId(Long id);
    UserDetailsDto changePassword(ChangePasswordRequestDto requestDto);
    UserDetailsDto changePasswordByAdmin(ChangePasswordByAdminRequestDto requestDto);
}