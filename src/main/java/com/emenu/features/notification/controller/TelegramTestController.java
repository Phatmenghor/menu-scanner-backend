package com.emenu.features.notification.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.notification.service.TelegramNotificationService;
import com.emenu.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/test/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramTestController {

    private final TelegramNotificationService telegramNotificationService;
    private final UserRepository userRepository;

    /**
     * Simple test to verify Telegram group messaging works
     */
    @PostMapping("/group-test")
    public ResponseEntity<ApiResponse<String>> testGroupMessage(@RequestParam String message) {
        log.info("🧪 Testing Telegram group message: {}", message);
        
        try {
            String testMessage = "🧪 <b>Test Message</b>\n\n" + message + "\n\n<i>Sent from Swagger API test</i>";
            telegramNotificationService.sendGroupMessage(testMessage);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Test message sent to Telegram group successfully", 
                "Message: " + message
            ));
        } catch (Exception e) {
            log.error("❌ Failed to send test message: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(
                "Failed to send test message: " + e.getMessage()
            ));
        }
    }

    /**
     * Test platform user creation notification with existing users
     */
    @PostMapping("/platform-user-notification")
    public ResponseEntity<ApiResponse<String>> testPlatformUserNotification(
            @RequestParam UUID newUserId,
            @RequestParam UUID createdByUserId) {
        
        log.info("🧪 Testing platform user creation notification: newUser={}, createdBy={}", 
                newUserId, createdByUserId);
        
        try {
            User newUser = userRepository.findByIdAndIsDeletedFalse(newUserId)
                    .orElseThrow(() -> new RuntimeException("New user not found: " + newUserId));
            
            User createdBy = userRepository.findByIdAndIsDeletedFalse(createdByUserId)
                    .orElseThrow(() -> new RuntimeException("Creator user not found: " + createdByUserId));
            
            telegramNotificationService.sendPlatformUserCreationNotification(newUser, createdBy);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Platform user creation notification sent successfully",
                String.format("Notification sent for user: %s (created by: %s)", 
                        newUser.getUserIdentifier(), createdBy.getUserIdentifier())
            ));
            
        } catch (Exception e) {
            log.error("❌ Failed to send platform user notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(
                "Failed to send notification: " + e.getMessage()
            ));
        }
    }

    /**
     * Check Telegram configuration
     */
    @GetMapping("/config-check")
    public ResponseEntity<ApiResponse<Object>> checkTelegramConfig() {
        log.info("🔍 Checking Telegram configuration");
        
        try {
            boolean enabled = telegramNotificationService.isTelegramEnabled();
            
            java.util.Map<String, Object> config = new java.util.HashMap<>();
            config.put("telegramEnabled", enabled);
            config.put("status", enabled ? "✅ Ready" : "❌ Disabled");
            
            return ResponseEntity.ok(ApiResponse.success(
                "Telegram configuration checked", 
                config
            ));
            
        } catch (Exception e) {
            log.error("❌ Failed to check config: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(
                "Failed to check configuration: " + e.getMessage()
            ));
        }
    }
}