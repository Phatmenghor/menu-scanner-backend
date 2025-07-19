package com.emenu.features.user_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());
        AuthenticationResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "User registration")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for: {}", request.getEmail());
        AuthenticationResponse response = authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        log.info("Email verification attempt");
        authenticationService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        log.info("Password reset request for: {}", email);
        authenticationService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String token, @RequestParam String newPassword) {
        log.info("Password reset attempt");
        authenticationService.resetPassword(token, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change attempt");
        authenticationService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout attempt");
        authenticationService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}