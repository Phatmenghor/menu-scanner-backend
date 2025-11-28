package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.request.*;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.TelegramAuthResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.service.AuthService;
import com.emenu.features.auth.service.UserService;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    // ===== TRADITIONAL AUTHENTICATION =====

    /**
     * Traditional user login with userIdentifier/password
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("🔐 Traditional login request for: {}", request.getUserIdentifier());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * User logout (both traditional and Telegram users)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    /**
     * Customer self-registration (traditional registration)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("📝 Customer registration request for: {}", request.getUserIdentifier());
        UserResponse response = authService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer registration successful", response));
    }

    /**
     * Change password for current authenticated user
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("🔑 Password change request received");
        UserResponse response = authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", response));
    }

    /**
     * Admin reset password for any user
     */
    @PostMapping("/admin/reset-password")
    public ResponseEntity<ApiResponse<UserResponse>> adminResetPassword(
            @Valid @RequestBody AdminPasswordResetRequest request) {
        log.info("🔑 Admin password reset request for user: {}", request.getUserId());
        UserResponse response = authService.adminResetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", response));
    }
}