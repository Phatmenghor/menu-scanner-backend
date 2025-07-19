package com.emenu.features.usermanagement.service;

import com.emenu.features.usermanagement.dto.request.LoginRequest;
import com.emenu.features.usermanagement.dto.request.RefreshTokenRequest;
import com.emenu.features.usermanagement.dto.request.RegisterRequest;
import com.emenu.features.usermanagement.dto.response.AuthenticationResponse;

public interface AuthenticationService {
    
    // Core authentication methods
    AuthenticationResponse login(LoginRequest request);
    AuthenticationResponse register(RegisterRequest request);
    AuthenticationResponse refreshToken(RefreshTokenRequest request);
    void logout(String token);
    
    // Email verification
    void verifyEmail(String token);
    void resendVerificationEmail(String email);
    
    // Phone verification
    void requestPhoneVerification(String email);
    void verifyPhone(String email, String code);
    
    // Password management
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
