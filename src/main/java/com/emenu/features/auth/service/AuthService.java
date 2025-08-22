package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.*;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;

public interface AuthService {

    // ===== TRADITIONAL AUTHENTICATION =====
    LoginResponse login(LoginRequest request);
    void logout(String token);
    UserResponse registerCustomer(RegisterRequest request);

    // ===== PASSWORD MANAGEMENT =====
    UserResponse changePassword(PasswordChangeRequest request);
    UserResponse adminResetPassword(AdminPasswordResetRequest request);
}