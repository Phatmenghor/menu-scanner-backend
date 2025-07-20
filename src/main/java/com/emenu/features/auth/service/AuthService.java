package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;

public interface AuthService {
    LoginResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    void logout();
    void changePassword(PasswordChangeRequest request);
    void forgotPassword(String email);
    UserResponse getCurrentUserProfile();
    UserResponse updateCurrentUserProfile(UserUpdateRequest request);
}
