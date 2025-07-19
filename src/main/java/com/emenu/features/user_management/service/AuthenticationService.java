package com.emenu.features.user_management.service;

import com.emenu.features.user_management.dto.request.ChangePasswordRequest;
import com.emenu.features.user_management.dto.request.LoginRequest;
import com.emenu.features.user_management.dto.request.RegisterRequest;
import com.emenu.features.user_management.dto.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse login(LoginRequest request);
    AuthenticationResponse register(RegisterRequest request);
    void verifyEmail(String token);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
    void changePassword(ChangePasswordRequest request);
    void logout(String token);
}
