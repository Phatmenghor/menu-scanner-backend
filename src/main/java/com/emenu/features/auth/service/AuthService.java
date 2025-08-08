package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.*;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.TelegramLoginResponse;
import com.emenu.features.auth.dto.response.TelegramRegisterResponse;
import com.emenu.features.auth.dto.response.UserResponse;

public interface AuthService {

    // ===== TRADITIONAL AUTHENTICATION =====
    LoginResponse login(LoginRequest request);
    void logout(String token);
    UserResponse registerCustomer(RegisterRequest request);

    // ===== TELEGRAM AUTHENTICATION =====
    TelegramLoginResponse loginWithTelegram(TelegramLoginRequest request);
    TelegramRegisterResponse registerWithTelegram(TelegramRegisterRequest request);
    
    // ===== TELEGRAM ACCOUNT LINKING =====
    void linkTelegramToCurrentUser(TelegramLoginRequest telegramData);
    void unlinkTelegramFromCurrentUser();

    // ===== PASSWORD MANAGEMENT =====
    UserResponse changePassword(PasswordChangeRequest request);
    UserResponse adminResetPassword(AdminPasswordResetRequest request);
}