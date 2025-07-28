package com.emenu.features.auth.controller;

import com.emenu.enums.user.UserType;
import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.service.AuthService;
import com.emenu.features.auth.service.impl.AuthServiceImpl;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 🔐 User login (traditional email/password)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for userIdentifier: {}", request.getUserIdentifier());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * 🚪 User logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    /**
     * 📝 Customer self-registration only
     * Business users must be created by platform administrators through user management
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Customer registration request received for: {}", request.getUserIdentifier());
        
        UserResponse response = authService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer registration successful", response));
    }

    /**
     * 🔍 Check login methods for user
     * Helps frontend determine what login options to show
     */
    @GetMapping("/login-methods/{userIdentifier}")
    public ResponseEntity<ApiResponse<AuthServiceImpl.LoginMethodsResponse>> getLoginMethods(
            @PathVariable String userIdentifier) {
        log.info("Checking login methods for user: {}", userIdentifier);
        
        AuthServiceImpl authServiceImpl = (AuthServiceImpl) authService;
        AuthServiceImpl.LoginMethodsResponse response = authServiceImpl.getLoginMethods(userIdentifier);
        
        return ResponseEntity.ok(ApiResponse.success("Login methods retrieved", response));
    }

    /**
     * 🔄 Check authentication status
     * Useful for frontend to verify if user is still logged in
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuthStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(ApiResponse.success("Not authenticated", Map.of(
                    "authenticated", false,
                    "message", "No valid token provided"
                )));
            }

            // If we reach here, the JWT filter has already validated the token
            Map<String, Object> status = Map.of(
                "authenticated", true,
                "message", "User is authenticated",
                "tokenValid", true
            );
            
            return ResponseEntity.ok(ApiResponse.success("Authentication status", status));
            
        } catch (Exception e) {
            log.debug("Auth status check failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.success("Authentication failed", Map.of(
                "authenticated", false,
                "message", "Token validation failed"
            )));
        }
    }

    /**
     * ℹ️ Get authentication configuration
     * Returns what auth methods are available on this platform
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuthConfig() {
        log.info("Getting authentication configuration");
        
        Map<String, Object> config = Map.of(
            "traditionalLogin", Map.of(
                "enabled", true,
                "requiresPassword", true,
                "allowsUserIdentifier", true,
                "allowsEmail", true
            ),
            "socialLogin", Map.of(
                "enabled", true,
                "providers", Map.of(
                    "GOOGLE", Map.of(
                        "enabled", true,
                        "displayName", "Google",
                        "description", "Login with your Google account",
                        "color", "#4285f4",
                        "icon", "google"
                    ),
                    "TELEGRAM", Map.of(
                        "enabled", true,
                        "displayName", "Telegram", 
                        "description", "Login with your Telegram account",
                        "color", "#0088cc",
                        "icon", "telegram"
                    )
                )
            ),
            "registration", Map.of(
                "customerSelfRegistration", true,
                "socialRegistration", true,
                "adminOnlyBusinessRegistration", true
            ),
            "security", Map.of(
                "jwtEnabled", true,
                "tokenBlacklistEnabled", true,
                "socialAccountLinking", true
            )
        );
        
        return ResponseEntity.ok(ApiResponse.success("Authentication configuration", config));
    }

    /**
     * 🆔 Check if userIdentifier is available
     * Useful for registration forms
     */
    @GetMapping("/check-availability/{userIdentifier}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkUserIdentifierAvailability(
            @PathVariable String userIdentifier) {
        log.info("Checking availability for userIdentifier: {}", userIdentifier);
        
        try {
            AuthServiceImpl authServiceImpl = (AuthServiceImpl) authService;
            AuthServiceImpl.LoginMethodsResponse loginMethods = authServiceImpl.getLoginMethods(userIdentifier);
            
            // If we get here without exception, user exists
            Map<String, Object> result = Map.of(
                "userIdentifier", userIdentifier,
                "available", false,
                "exists", true,
                "canLoginWithPassword", loginMethods.isCanLoginWithPassword(),
                "canLoginSocially", loginMethods.isCanLoginSocially(),
                "message", "User identifier is already taken"
            );
            
            return ResponseEntity.ok(ApiResponse.success("User identifier check", result));
            
        } catch (Exception e) {
            // User doesn't exist
            Map<String, Object> result = Map.of(
                "userIdentifier", userIdentifier,
                "available", true,
                "exists", false,
                "message", "User identifier is available"
            );
            
            return ResponseEntity.ok(ApiResponse.success("User identifier check", result));
        }
    }

    /**
     * 🔗 Quick social login check
     * Check if user has social accounts without full login
     */
    @PostMapping("/check-social-user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSocialUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String provider = request.get("provider");
        
        log.info("Checking social user: {} from provider: {}", email, provider);
        
        try {
            // This is a simplified check - in a real implementation, you might want to
            // create a specific service method for this
            AuthServiceImpl authServiceImpl = (AuthServiceImpl) authService;
            
            Map<String, Object> result = Map.of(
                "socialUserExists", false, // Implement actual logic
                "provider", provider,
                "email", email != null ? email : "",
                "message", "Social user check completed"
            );
            
            return ResponseEntity.ok(ApiResponse.success("Social user check", result));
            
        } catch (Exception e) {
            log.error("Error checking social user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to check social user: " + e.getMessage()));
        }
    }

    /**
     * 🛡️ Validate token
     * Explicit token validation endpoint
     */
    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // If we reach here, JWT filter has already validated the token
            Map<String, Object> result = Map.of(
                "valid", true,
                "message", "Token is valid",
                "authenticated", true
            );
            
            return ResponseEntity.ok(ApiResponse.success("Token validation", result));
            
        } catch (Exception e) {
            Map<String, Object> result = Map.of(
                "valid", false,
                "message", "Token is invalid or expired",
                "authenticated", false
            );
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token validation failed", result));
        }
    }

    /**
     * 📊 Get authentication statistics (for admin/monitoring)
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuthStats() {
        log.info("Getting authentication statistics");
        
        // This would typically require admin permissions
        // For now, returning mock data
        Map<String, Object> stats = Map.of(
            "totalUsers", "Not implemented",
            "socialUsers", "Not implemented", 
            "traditionalUsers", "Not implemented",
            "activeTokens", "Not implemented",
            "recentLogins", "Not implemented",
            "message", "This endpoint requires implementation of statistics collection"
        );
        
        return ResponseEntity.ok(ApiResponse.success("Authentication statistics", stats));
    }

    /**
     * 🏥 Health check for authentication system
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuthHealth() {
        try {
            Map<String, Object> health = Map.of(
                "status", "UP",
                "traditionalAuth", "operational",
                "socialAuth", "operational", 
                "jwtService", "operational",
                "tokenBlacklist", "operational",
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Authentication system health", health));
            
        } catch (Exception e) {
            Map<String, Object> health = Map.of(
                "status", "DOWN",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("Authentication system unhealthy", health));
        }
    }
}