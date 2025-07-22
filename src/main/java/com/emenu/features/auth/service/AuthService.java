package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.AdminPasswordResetRequest;
import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;

public interface AuthService {

    // Authentication
    LoginResponse login(LoginRequest request);

    void logout(String token); //

    UserResponse register(RegisterRequest request); //

    // Password Management
    UserResponse changePassword(PasswordChangeRequest request); //

    UserResponse adminResetPassword(AdminPasswordResetRequest request);
}