package com.emenu.features.usermanagement.controller;

import com.emenu.features.usermanagement.dto.request.LoginRequest;
import com.emenu.features.usermanagement.dto.request.RefreshTokenRequest;
import com.emenu.features.usermanagement.dto.request.RegisterRequest;
import com.emenu.features.usermanagement.dto.response.AuthenticationResponse;
import com.emenu.features.usermanagement.service.AuthenticationService;
import com.emenu.shared.constants.Messages;
import com.emenu.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Authentication operations")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return access token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to login user with email: {}", request.getEmail());
        
        AuthenticationResponse response = authenticationService.login(request);
        
        return ResponseEntity.ok(ApiResponse.success(Messages.LOGIN_SUCCESS, response));
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST request to register user with email: {}", request.getEmail());
        
        AuthenticationResponse response = authenticationService.register(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please verify your email.", response));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("REST request to refresh token");
        
        AuthenticationResponse response = authenticationService.refreshToken(request);
        
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate token")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("REST request to logout user");
        
        String token = authHeader.replace("Bearer ", "");
        authenticationService.logout(token);
        
        return ResponseEntity.ok(ApiResponse.success(Messages.LOGOUT_SUCCESS, null));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email with token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "Verification token") @RequestParam String token) {
        log.info("REST request to verify email with token: {}", token);
        
        authenticationService.verifyEmail(token);
        
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Parameter(description = "User email") @RequestParam String email) {
        log.info("REST request to reset password for email: {}", email);
        
        authenticationService.forgotPassword(email);
        
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password with token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "Reset token") @RequestParam String token,
            @Parameter(description = "New password") @RequestParam String newPassword) {
        log.info("REST request to reset password with token: {}", token);
        
        authenticationService.resetPassword(token, newPassword);
        
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}