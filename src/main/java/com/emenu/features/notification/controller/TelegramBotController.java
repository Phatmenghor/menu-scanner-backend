package com.emenu.features.notification.controller;

import com.emenu.features.auth.service.AuthService;
import com.emenu.features.notification.dto.request.MultiRecipientNotificationRequest;
import com.emenu.features.notification.dto.request.TelegramBotCallbackRequest;
import com.emenu.features.notification.dto.response.NotificationSendResult;
import com.emenu.features.notification.dto.response.TelegramBotResponse;
import com.emenu.features.notification.models.TelegramUserSession;
import com.emenu.features.notification.repository.TelegramUserSessionRepository;
import com.emenu.features.notification.service.TelegramBotService;
import com.emenu.features.notification.service.TelegramService;
import com.emenu.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/telegram/bot")
@RequiredArgsConstructor
@Slf4j
public class TelegramBotController {

    private final TelegramService telegramService;
    private final TelegramBotService telegramBotService;
    private final TelegramUserSessionRepository sessionRepository;
    private final AuthService authService;

    // ===== BOT WEBHOOK & CALLBACKS =====

    /**
     * Telegram Bot Webhook Endpoint
     * This receives updates from Telegram when users interact with the bot
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<String>> handleWebhook(@RequestBody Map<String, Object> update) {
        log.info("📨 Received Telegram webhook update");
        
        try {
            TelegramBotResponse response = telegramBotService.processUpdate(update);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(ApiResponse.success("Webhook processed successfully",
                        response.getMessage()));
            } else {
                return ResponseEntity.ok(ApiResponse.error("Webhook processing failed: " + response.getMessage()));
            }
        } catch (Exception e) {
            log.error("❌ Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Webhook processing error: " + e.getMessage()));
        }
    }

    /**
     * Handle bot callback queries (inline keyboard buttons)
     */
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<TelegramBotResponse>> handleCallback(
            @RequestBody TelegramBotCallbackRequest request) {
        log.info("🎯 Processing Telegram callback from user: {}", request.getTelegramUserId());
        
        try {
            TelegramBotResponse response = telegramBotService.processCallback(request);
            return ResponseEntity.ok(ApiResponse.success("Callback processed", response));
        } catch (Exception e) {
            log.error("❌ Error processing callback: {}", e.getMessage(), e);
            TelegramBotResponse errorResponse = TelegramBotResponse.builder()
                    .success(false)
                    .message("Error processing callback: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(ApiResponse.error("Callback processing failed", errorResponse));
        }
    }

    // ===== ADMIN TESTING ENDPOINTS =====

    /**
     * Test bot connection
     */
    @GetMapping("/test-connection")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> testBotConnection() {
        log.info("🧪 Testing Telegram bot connection");
        boolean isConnected = telegramService.testConnection();
        
        String message = isConnected ? "Bot connection successful" : "Bot connection failed";
        return ResponseEntity.ok(ApiResponse.success(message, isConnected));
    }

    /**
     * Send test message to all platform users
     */
    @PostMapping("/test-broadcast")
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<NotificationSendResult>> testBroadcast(
            @RequestBody Map<String, String> request) {
        log.info("📢 Testing broadcast message");
        
        String testMessage = request.getOrDefault("message", "🧪 Test broadcast message from Cambodia E-Menu Platform");
        
        try {
            MultiRecipientNotificationRequest notificationRequest = MultiRecipientNotificationRequest.builder()
                    .notificationType("TEST_BROADCAST")
                    .title("Test Broadcast")
                    .message(testMessage + "\n\n📅 Sent at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .includePlatformUsers(true)
                    .includeBusinessOwners(false)
                    .includeCustomers(false)
                    .sendImmediate(true)
                    .build();
            
            CompletableFuture<NotificationSendResult> resultFuture = telegramService.sendMultiRecipientNotification(notificationRequest);
            NotificationSendResult result = resultFuture.get();
            
            return ResponseEntity.ok(ApiResponse.success("Broadcast test completed", result));
        } catch (Exception e) {
            log.error("❌ Error in broadcast test: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Broadcast test failed: " + e.getMessage()));
        }
    }

    /**
     * Send test notification for new user registration
     */
    @PostMapping("/test-user-notification")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<String>> testUserNotification() {
        log.info("👤 Testing user registration notification");
        
        try {
            CompletableFuture<Boolean> result = telegramService.sendUserRegisteredNotification(
                    "test_user_" + System.currentTimeMillis(),
                    "Test User (Generated)",
                    "CUSTOMER",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );
            
            boolean sent = result.get();
            String message = sent ? "User notification test sent successfully" : "User notification test failed";
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("❌ Error in user notification test: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("User notification test failed: " + e.getMessage()));
        }
    }

    /**
     * Send test notification for new business registration
     */
    @PostMapping("/test-business-notification")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<String>> testBusinessNotification() {
        log.info("🏪 Testing business registration notification");
        
        try {
            CompletableFuture<Boolean> result = telegramService.sendBusinessRegisteredNotification(
                    "Test Restaurant " + System.currentTimeMillis(),
                    "Test Owner",
                    "test@cambodia-emenu.com",
                    "070 411 260",
                    "test-restaurant-" + System.currentTimeMillis(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );
            
            boolean sent = result.get();
            String message = sent ? "Business notification test sent successfully" : "Business notification test failed";
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("❌ Error in business notification test: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Business notification test failed: " + e.getMessage()));
        }
    }

    // ===== BOT STATISTICS & MANAGEMENT =====

    /**
     * Get bot statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBotStats() {
        log.info("📊 Getting bot statistics");
        
        try {
            Map<String, Object> stats = telegramBotService.getBotStatistics();
            return ResponseEntity.ok(ApiResponse.success("Bot statistics retrieved", stats));
        } catch (Exception e) {
            log.error("❌ Error getting bot statistics: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Failed to get bot statistics: " + e.getMessage()));
        }
    }

    /**
     * Get active Telegram sessions
     */
    @GetMapping("/sessions")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<TelegramUserSession>>> getActiveSessions() {
        log.info("📋 Getting active Telegram sessions");
        
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(24); // Active in last 24 hours
            List<TelegramUserSession> sessions = sessionRepository.findRecentActiveSessions(since);
            
            return ResponseEntity.ok(ApiResponse.success(
                    String.format("Found %d active sessions in last 24 hours", sessions.size()), 
                    sessions));
        } catch (Exception e) {
            log.error("❌ Error getting sessions: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Failed to get sessions: " + e.getMessage()));
        }
    }

    /**
     * Cleanup inactive sessions
     */
    @PostMapping("/cleanup-sessions")
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<String>> cleanupInactiveSessions() {
        log.info("🧹 Cleaning up inactive Telegram sessions");
        
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30); // 30 days old
            int deletedCount = sessionRepository.deleteInactiveUnregisteredSessions(cutoff);
            
            String message = String.format("Cleaned up %d inactive sessions older than 30 days", deletedCount);
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("❌ Error cleaning up sessions: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Session cleanup failed: " + e.getMessage()));
        }
    }

    // ===== DIRECT MESSAGING =====

    /**
     * Send direct message to a specific Telegram user
     */
    @PostMapping("/send-message")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<String>> sendDirectMessage(@RequestBody Map<String, String> request) {
        String chatId = request.get("chatId");
        String message = request.get("message");
        String context = request.getOrDefault("context", "Admin Message");
        
        if (chatId == null || message == null) {
            return ResponseEntity.ok(ApiResponse.error("ChatId and message are required"));
        }
        
        log.info("💬 Sending direct message to chat: {}", chatId);
        
        try {
            CompletableFuture<Boolean> result = telegramService.sendDirectMessageToUser(chatId, message, context);
            boolean sent = result.get();
            
            String responseMessage = sent ? "Message sent successfully" : "Failed to send message";
            return ResponseEntity.ok(ApiResponse.success(responseMessage, responseMessage));
        } catch (Exception e) {
            log.error("❌ Error sending direct message: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send message: " + e.getMessage()));
        }
    }
}