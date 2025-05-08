package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.AuthResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserResponseDto;
import com.menghor.ksit.feature.auth.dto.request.LoginRequestDto;

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
     * Change user's student password (by user themselves)
     */
    StudentUserResponseDto changePasswordStudent(ChangePasswordRequestDto requestDto);

    /**
     * Change user's staff password (by admin)
     */
    StaffUserResponseDto changePasswordStaffByAdmin(ChangePasswordByAdminRequestDto requestDto);

    /**
     * Change user's student password (by admin)
     */
    StudentUserResponseDto changePasswordStudentByAdmin(ChangePasswordByAdminRequestDto requestDto);

    /**
     * Token refresh mechanism
     */
    AuthResponseDto refreshToken(String refreshToken);
}