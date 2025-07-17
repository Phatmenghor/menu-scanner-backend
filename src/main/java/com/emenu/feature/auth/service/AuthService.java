package com.emenu.feature.auth.service;

import com.emenu.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.emenu.feature.auth.dto.request.ChangePasswordRequestDto;
import com.emenu.feature.auth.dto.request.LoginRequestDto;

public interface AuthService {
    /**
     * Authenticate a user and generate JWT token
     */
    AuthResponseDto login(LoginRequestDto loginRequestDto);

    /**
     * Change user's staff password (by user themselves)
     */
    StaffUserResponseDto changePasswordStaff(ChangePasswordRequestDto requestDto);

    /**
     * Change user's student password (by admin)
     */
    StudentUserResponseDto changePasswordByAdmin(ChangePasswordByAdminRequestDto requestDto);

    /**
     * Token refresh mechanism
     */
    AuthResponseDto refreshToken(String refreshToken);
}