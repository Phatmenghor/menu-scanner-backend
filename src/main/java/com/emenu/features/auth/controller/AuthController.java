package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.request.*;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.TelegramAuthResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.service.AuthService;
import com.emenu.features.auth.service.impl.TelegramAuthServiceImpl;
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
    private final TelegramAuthServiceImpl telegramAuthServiceImpl;

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

    // ===== TELEGRAM AUTHENTICATION =====

    /**
     * Telegram login for existing users
     */
    @PostMapping("/telegram/login")
    public ResponseEntity<ApiResponse<TelegramAuthResponse>> loginWithTelegram(
            @Valid @RequestBody TelegramAuthRequest request) {
        log.info("📱 Telegram login request for user: {}", request.getTelegramUserId());
        TelegramAuthResponse response = telegramAuthServiceImpl.loginWithTelegram(request);
        return ResponseEntity.ok(ApiResponse.success("Telegram login successful", response));
    }

    /**
     * Telegram registration for new customers
     */
    @PostMapping("/telegram/register")
    public ResponseEntity<ApiResponse<TelegramAuthResponse>> registerCustomerWithTelegram(
            @Valid @RequestBody TelegramAuthRequest request) {
        log.info("📱 Telegram customer registration for user: {}", request.getTelegramUserId());
        TelegramAuthResponse response = telegramAuthServiceImpl.registerCustomerWithTelegram(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Telegram customer registration successful", response));
    }

    /**
     * Check if Telegram user is already registered
     */
    @GetMapping("/telegram/check/{telegramUserId}")
    public ResponseEntity<ApiResponse<Boolean>> checkTelegramUser(@PathVariable Long telegramUserId) {
        log.info("🔍 Checking if Telegram user {} is registered", telegramUserId);
        boolean isLinked = telegramAuthServiceImpl.isTelegramLinked(telegramUserId);
        
        String message = isLinked ? "Telegram user is registered" : "Telegram user is not registered";
        return ResponseEntity.ok(ApiResponse.success(message, isLinked));
    }

    // ===== TELEGRAM ACCOUNT LINKING =====

    /**
     * Link Telegram account to current authenticated user
     */
    @PostMapping("/telegram/link")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> linkTelegramAccount(
            @Valid @RequestBody TelegramLinkRequest request) {
        log.info("🔗 Linking Telegram account: {}", request.getTelegramUserId());
        telegramAuthServiceImpl.linkTelegramToCurrentUser(request);
        return ResponseEntity.ok(ApiResponse.success("Telegram account linked successfully", 
                "Your Telegram account has been linked. You can now login with Telegram."));
    }

    /**
     * Unlink Telegram account from current authenticated user
     */
    @PostMapping("/telegram/unlink")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> unlinkTelegramAccount() {
        log.info("🔓 Unlinking Telegram account from current user");
        telegramAuthServiceImpl.unlinkTelegramFromCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Telegram account unlinked successfully", 
                "Your Telegram account has been unlinked. You can now only login with username/password."));
    }

    // ===== PASSWORD MANAGEMENT =====

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
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> adminResetPassword(
            @Valid @RequestBody AdminPasswordResetRequest request) {
        log.info("🔑 Admin password reset request for user: {}", request.getUserId());
        UserResponse response = authService.adminResetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", response));
    }

    // ===== AUTHENTICATION STATUS =====

    /**
     * Check authentication status and get user info
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> getCurrentAuthStatus() {
        return ResponseEntity.ok(ApiResponse.success("User is authenticated", 
                "You are successfully authenticated and can access protected resources."));
    }

    /**
     * Test endpoint to verify JWT token is working
     */
    @GetMapping("/test")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> testAuth() {
        return ResponseEntity.ok(ApiResponse.success("Authentication test successful", 
                "JWT token is valid and authentication is working properly."));
    }

    /**
     * Get current user's Telegram status
     */
    @GetMapping("/telegram/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> getTelegramStatus() {
        // This would need to be implemented to check current user's Telegram link status
        return ResponseEntity.ok(ApiResponse.success("Telegram status retrieved", false));
    }
}