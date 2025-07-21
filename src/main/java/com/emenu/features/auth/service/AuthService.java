package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;

import java.util.UUID;

public interface AuthService {

    // Authentication
    LoginResponse login(LoginRequest request);
    void logout(String token);
    LoginResponse refreshToken(String refreshToken);

    // Registration
    UserResponse register(RegisterRequest request);
    UserResponse registerBusinessOwner(RegisterRequest request);
    UserResponse registerCustomer(RegisterRequest request);

    // Password Management
    void changePassword(PasswordChangeRequest request);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);

    // Email Verification
    void sendEmailVerification(UUID userId);
    void verifyEmail(String token);

    // Account Management
    void lockAccount(UUID userId);
    void unlockAccount(UUID userId);
    void suspendAccount(UUID userId);
    void activateAccount(UUID userId);
}