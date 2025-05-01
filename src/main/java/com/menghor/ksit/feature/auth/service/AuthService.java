package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.request.UserRegisterDto;
import com.menghor.ksit.feature.auth.dto.request.StudentRegisterDto;
import com.menghor.ksit.feature.auth.dto.resposne.AuthResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.LoginResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsDto;

public interface AuthService {
    /**
     * Authenticate a user and generate JWT token
     */
    AuthResponseDto login(LoginResponseDto loginResponseDto);

    /**
     * Student registration (Open to all)
     */
    UserDetailsDto registerStudent(StudentRegisterDto registerDto);

    /**
     * Advanced registration for staff, admin, developer
     * Controlled by role and authorization
     */
    UserDetailsDto registerAdvanced(UserRegisterDto registerDto);

    /**
     * Token refresh mechanism
     */
    AuthResponseDto refreshToken(String refreshToken);
}