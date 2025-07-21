package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.service.AuthService;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * User login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * User logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    /**
     * Customer registration
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    /**
     * Business owner registration
     */
    @PostMapping("/register/business-owner")
    public ResponseEntity<ApiResponse<UserResponse>> registerBusinessOwner(@Valid @RequestBody RegisterRequest request) {
        log.info("Business owner registration request received for email: {}", request.getEmail());
        UserResponse response = authService.registerBusinessOwner(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business owner registration successful", response));
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.info("Password change request received");
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    /**
     * Forgot password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        log.info("Forgot password request for email: {}", email);
        authService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success("Password reset instructions sent to your email", null));
    }

    /**
     * Reset password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestParam String token,
                                                           @RequestParam String newPassword) {
        log.info("Password reset request with token");
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
    }

    /**
     * Send email verification
     */
    @PostMapping("/verify-email/send")
    public ResponseEntity<ApiResponse<Void>> sendEmailVerification() {
        log.info("Email verification send request");
        // Would get current user ID from security context
        authService.sendEmailVerification(UUID.randomUUID());
        return ResponseEntity.ok(ApiResponse.success("Verification email sent", null));
    }

    /**
     * Verify email
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        log.info("Email verification request with token");
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    /**
     * Refresh token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestParam String refreshToken) {
        log.info("Refresh token request");
        LoginResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    // Admin-only endpoints for account management

    /**
     * Lock user account
     */
    @PostMapping("/admin/lock-account/{userId}")
    public ResponseEntity<ApiResponse<Void>> lockAccount(@PathVariable UUID userId) {
        log.info("Account lock request for user: {}", userId);
        authService.lockAccount(userId);
        return ResponseEntity.ok(ApiResponse.success("Account locked successfully", null));
    }

    /**
     * Unlock user account
     */
    @PostMapping("/admin/unlock-account/{userId}")
    public ResponseEntity<ApiResponse<Void>> unlockAccount(@PathVariable UUID userId) {
        log.info("Account unlock request for user: {}", userId);
        authService.unlockAccount(userId);
        return ResponseEntity.ok(ApiResponse.success("Account unlocked successfully", null));
    }

    /**
     * Suspend user account
     */
    @PostMapping("/admin/suspend-account/{userId}")
    public ResponseEntity<ApiResponse<Void>> suspendAccount(@PathVariable UUID userId) {
        log.info("Account suspension request for user: {}", userId);
        authService.suspendAccount(userId);
        return ResponseEntity.ok(ApiResponse.success("Account suspended successfully", null));
    }

    /**
     * Activate user account
     */
    @PostMapping("/admin/activate-account/{userId}")
//    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateAccount(@PathVariable UUID userId) {
        log.info("Account activation request for user: {}", userId);
        authService.activateAccount(userId);
        return ResponseEntity.ok(ApiResponse.success("Account activated successfully", null));
    }
}