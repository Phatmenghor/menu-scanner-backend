package com.emenu.features.auth.controller;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.features.auth.dto.request.LinkSocialAccountRequest;
import com.emenu.features.auth.dto.request.SocialLoginRequest;
import com.emenu.features.auth.dto.request.TelegramLoginData;
import com.emenu.features.auth.dto.response.SocialAccountResponse;
import com.emenu.features.auth.dto.response.SocialLoginResponse;
import com.emenu.features.auth.service.social.GoogleOAuth2Service;
import com.emenu.features.auth.service.social.SocialLoginService;
import com.emenu.features.auth.service.social.TelegramLoginService;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/social")
@RequiredArgsConstructor
@Slf4j
public class SocialLoginController {

    private final SocialLoginService socialLoginService;
    private final GoogleOAuth2Service googleOAuth2Service;
    private final TelegramLoginService telegramLoginService;

    /**
     * 🔐 Main social login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        log.info("🔐 Social login request received for provider: {}", request.getProvider());
        
        SocialLoginResponse response = socialLoginService.processSocialLogin(request);
        
        String message = response.isNewUser() ? 
            "Account created and login successful" : 
            "Social login successful";
            
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * 🔗 Link social account to current user
     */
    @PostMapping("/link")
    public ResponseEntity<ApiResponse<SocialAccountResponse>> linkSocialAccount(@Valid @RequestBody LinkSocialAccountRequest request) {
        log.info("🔗 Link social account request for provider: {}", request.getProvider());
        
        SocialAccountResponse response = socialLoginService.linkSocialAccount(request);
        return ResponseEntity.ok(ApiResponse.success("Social account linked successfully", response));
    }

    /**
     * 📋 Get user's linked social accounts
     */
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<SocialAccountResponse>>> getUserSocialAccounts() {
        log.info("📋 Getting user's social accounts");
        
        List<SocialAccountResponse> accounts = socialLoginService.getUserSocialAccounts();
        return ResponseEntity.ok(ApiResponse.success("Social accounts retrieved successfully", accounts));
    }

    /**
     * ❌ Unlink social account
     */
    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<Void>> unlinkSocialAccount(@PathVariable UUID accountId) {
        log.info("❌ Unlink social account: {}", accountId);
        
        socialLoginService.unlinkSocialAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success("Social account unlinked successfully", null));
    }

    // ================================
    // PROVIDER-SPECIFIC ENDPOINTS
    // ================================

    /**
     * 🌐 Get Google OAuth2 authorization URL
     */
    @GetMapping("/google/auth-url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getGoogleAuthUrl(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String redirectUri) {
        
        log.info("🌐 Generating Google auth URL");
        
        String authUrl = googleOAuth2Service.getGoogleAuthUrl(state, redirectUri);
        
        Map<String, String> response = Map.of(
            "authUrl", authUrl,
            "provider", "GOOGLE",
            "state", state != null ? state : ""
        );
        
        return ResponseEntity.ok(ApiResponse.success("Google auth URL generated", response));
    }

    /**
     * ⚡ Google OAuth2 callback (alternative to main login endpoint)
     */
    @PostMapping("/google/callback")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> googleCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String redirectUri) {
        
        log.info("⚡ Google OAuth2 callback received");
        
        SocialLoginRequest request = new SocialLoginRequest();
        request.setProvider(SocialProvider.GOOGLE);
        request.setCode(code);
        request.setState(state);
        request.setRedirectUri(redirectUri);
        
        SocialLoginResponse response = socialLoginService.processSocialLogin(request);
        
        String message = response.isNewUser() ? 
            "Account created and login successful via Google" : 
            "Google login successful";
            
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * 📱 Telegram login (Widget-based)
     */
    @PostMapping("/telegram/login")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> telegramLogin(@Valid @RequestBody TelegramLoginData telegramData) {
        log.info("📱 Telegram login request received for user ID: {}", telegramData.getId());
        
        SocialLoginRequest request = new SocialLoginRequest();
        request.setProvider(SocialProvider.TELEGRAM);
        request.setTelegramData(telegramData);
        
        SocialLoginResponse response = socialLoginService.processSocialLogin(request);
        
        String message = response.isNewUser() ? 
            "Account created and login successful via Telegram" : 
            "Telegram login successful";
            
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * 📱 Get Telegram login widget configuration
     */
    @GetMapping("/telegram/widget-config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTelegramWidgetConfig(
            @RequestParam(required = false) String redirectUrl) {
        
        log.info("📱 Getting Telegram widget configuration");
        
        Map<String, Object> config = Map.of(
            "provider", "TELEGRAM",
            "widgetUrl", telegramLoginService.getTelegramLoginWidgetUrl(redirectUrl != null ? redirectUrl : ""),
            "instructions", Map.of(
                "step1", "Click the Telegram login button on your frontend",
                "step2", "Authorize with Telegram",
                "step3", "Send the received data to /api/v1/auth/social/telegram/login",
                "note", "The frontend should handle the Telegram widget integration"
            )
        );
        
        return ResponseEntity.ok(ApiResponse.success("Telegram widget configuration", config));
    }

    // ================================
    // UTILITY ENDPOINTS
    // ================================

    /**
     * ℹ️ Get available social providers
     */
    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableProviders() {
        log.info("ℹ️ Getting available social providers");
        
        Map<String, Object> providers = Map.of(
            "GOOGLE", Map.of(
                "name", "Google",
                "description", "Login with your Google account",
                "enabled", true,
                "color", "#4285f4",
                "icon", "google"
            ),
            "TELEGRAM", Map.of(
                "name", "Telegram", 
                "description", "Login with your Telegram account",
                "enabled", true,
                "color", "#0088cc",
                "icon", "telegram"
            )
        );
        
        return ResponseEntity.ok(ApiResponse.success("Available social providers", providers));
    }

    /**
     * 🔄 Check social login status
     * Useful for frontend to know if user can login socially
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSocialLoginStatus() {
        log.info("🔄 Checking social login status");
        
        Map<String, Object> status = Map.of(
            "socialLoginEnabled", true,
            "availableProviders", List.of("GOOGLE", "TELEGRAM"),
            "customerRegistrationEnabled", true,
            "accountLinkingEnabled", true,
            "message", "Social login is fully operational"
        );
        
        return ResponseEntity.ok(ApiResponse.success("Social login status", status));
    }
}