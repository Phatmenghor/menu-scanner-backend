package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StudentRegisterRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StaffRegisterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.AuthResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsResponseDto;
import com.menghor.ksit.feature.auth.dto.request.LoginRequestDto;

public interface AuthService {
    /**
     * Authenticate a user and generate JWT token
     */
    AuthResponseDto login(LoginRequestDto loginRequestDto);

    /**
     * Change user's password (by user themselves)
     */
    UserDetailsResponseDto changePassword(ChangePasswordRequestDto requestDto);

    /**
     * Change user's password (by admin)
     */
    UserDetailsResponseDto changePasswordByAdmin(ChangePasswordByAdminRequestDto requestDto);

    /**
     * Token refresh mechanism
     */
    AuthResponseDto refreshToken(String refreshToken);
}