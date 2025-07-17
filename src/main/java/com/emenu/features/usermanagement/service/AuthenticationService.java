package com.emenu.features.usermanagement.service;

import com.emenu.features.usermanagement.dto.request.LoginRequest;
import com.emenu.features.usermanagement.dto.request.RefreshTokenRequest;
import com.emenu.features.usermanagement.dto.request.RegisterRequest;
import com.emenu.features.usermanagement.dto.response.AuthenticationResponse;

public interface AuthenticationService {
    
    AuthenticationResponse login(LoginRequest request);
    
    AuthenticationResponse register(RegisterRequest request);
    
    AuthenticationResponse refreshToken(RefreshTokenRequest request);
    
    void logout(String token);
    
    void verifyEmail(String token);
    
    void forgotPassword(String email);
    
    void resetPassword(String token, String newPassword);
}
