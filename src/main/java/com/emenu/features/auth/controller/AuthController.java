package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.response.WelcomeMessageRequest;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.service.AuthService;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Welcome message sent!", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("Logout request");
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.info("Password change request");
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        log.info("Forgot password request for email: {}", email);
        authService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success("Password reset instructions sent to your email", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        log.info("Password reset request");
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        log.info("Email verification request");
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestParam String email) {
        log.info("Resend verification request for email: {}", email);
        authService.resendVerification(email);
        return ResponseEntity.ok(ApiResponse.success("Verification email sent", null));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        log.info("Get profile request");
        UserResponse profile = authService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Update profile request");
        UserResponse profile = authService.updateCurrentUserProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }

    @PostMapping("/send-welcome-message")
    public ResponseEntity<ApiResponse<Void>> sendCustomWelcomeMessage(
            @Valid @RequestBody WelcomeMessageRequest request) {
        log.info("Sending custom welcome message");
        authService.sendCustomWelcomeMessage(request);
        return ResponseEntity.ok(ApiResponse.success("Welcome message sent successfully", null));
    }
}