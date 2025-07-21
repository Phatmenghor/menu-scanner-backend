package com.emenu.features.auth.controller;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.UserType;
import com.emenu.features.auth.dto.request.AdminPasswordResetRequest;
import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.AccountStatusUpdateRequest;
import com.emenu.features.auth.service.AuthService;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * Unified user registration for all user types
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for {} user: {}", request.getUserType(), request.getEmail());
        UserResponse response = authService.register(request);

        String userTypeMessage = getUserTypeMessage(request.getUserType());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userTypeMessage + " registration successful", response));
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<UserResponse>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.info("Password change request received");
        UserResponse response = authService.changePassword(request); // ✅ Now returns UserResponse
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", response));
    }

    /**
     * Reset password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<UserResponse>> resetPassword(@RequestParam String token,
                                                                   @RequestParam String newPassword) {
        log.info("Password reset request with token");
        UserResponse response = authService.resetPassword(token, newPassword); // ✅ Now returns UserResponse
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", response));
    }

    /**
     * Admin reset password
     */
    @PostMapping("/admin/reset-password")
    public ResponseEntity<ApiResponse<UserResponse>> adminResetPassword(@Valid @RequestBody AdminPasswordResetRequest request) {
        log.info("Admin password reset request for user: {}", request.getUserId());
        UserResponse response = authService.adminResetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", response));
    }

    /**
     * Update user account status (Lock/Unlock/Suspend/Activate)
     */
    @PostMapping("/admin/update-account-status")
    public ResponseEntity<ApiResponse<UserResponse>> updateAccountStatus(@Valid @RequestBody AccountStatusUpdateRequest request) {
        log.info("Account status update request for user: {} to status: {}", request.getUserId(), request.getAccountStatus());
        UserResponse response = authService.updateAccountStatus(request);

        String statusMessage = getStatusMessage(request.getAccountStatus());
        return ResponseEntity.ok(ApiResponse.success(statusMessage, response));
    }

    private String getUserTypeMessage(UserType userType) {
        return switch (userType) {
            case CUSTOMER -> "Customer";
            case BUSINESS_USER -> "Business user";
            case PLATFORM_USER -> "Platform user";
        };
    }

    private String getStatusMessage(AccountStatus accountStatus) {
        return switch (accountStatus) {
            case ACTIVE -> "Account activated successfully";
            case INACTIVE -> "Account deactivated successfully";
            case LOCKED -> "Account locked successfully";
            case SUSPENDED -> "Account suspended successfully";
        };
    }
}