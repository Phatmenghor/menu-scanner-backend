package com.emenu.features.notification.controller;

import com.emenu.features.notification.service.TelegramService;
import com.emenu.shared.dto.ApiResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramController {

    private final TelegramService telegramService;

    /**
     * Test telegram connection
     */
    @GetMapping("/test-connection")
    public ResponseEntity<ApiResponse<Boolean>> testConnection() {
        log.info("Testing Telegram connection");
        boolean isConnected = telegramService.testConnection();
        
        String message = isConnected ? "Telegram connection successful" : "Telegram connection failed";
        return ResponseEntity.ok(ApiResponse.success(message, isConnected));
    }

    /**
     * Send test message with request body
     */
    @PostMapping("/test-message")
    public ResponseEntity<ApiResponse<String>> sendTestMessage(@RequestBody TestMessageRequest request) {
        log.info("Sending test message to Telegram: {}", request.getMessage());
        
        String fullMessage = String.format("""
                🧪 *Test Message*
                
                %s
                
                📅 *Sent at:* %s
                🤖 *From:* Cambodia E-Menu Platform
                """, 
                request.getMessage(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        // ✅ FIXED: Use the correct method signature with chatId from config
        CompletableFuture<Boolean> result = telegramService.sendDirectMessageToUser(
                "1898032377", // Use the configured chat ID
                fullMessage, 
                "Test Message"
        );
        
        try {
            boolean sent = result.get();
            String responseMessage = sent ? "Test message sent successfully" : "Failed to send test message";
            return ResponseEntity.ok(ApiResponse.success(responseMessage, responseMessage));
        } catch (Exception e) {
            log.error("Error sending test message: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to send test message: " + e.getMessage()));
        }
    }

    /**
     * Send test message with query parameter (backward compatibility)
     */
    @PostMapping("/test-message-param")
    public ResponseEntity<ApiResponse<String>> sendTestMessageParam(@RequestParam String message) {
        TestMessageRequest request = new TestMessageRequest();
        request.setMessage(message);
        return sendTestMessage(request);
    }

    /**
     * Send test product notification
     */
    @PostMapping("/test-product-notification")
    public ResponseEntity<ApiResponse<String>> sendTestProductNotification() {
        log.info("Sending test product notification");
        
        CompletableFuture<Boolean> result = telegramService.sendProductCreatedNotification(
                "Test Product - Delicious Amok Fish 🐟",
                "Test Restaurant - Phat's Kitchen 🍽️",
                "15.50",
                "Traditional Khmer Cuisine",
                "PHAT_MENGHOR",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
        
        try {
            boolean sent = result.get();
            String message = sent ? "Test product notification sent successfully" : "Failed to send test product notification";
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("Error sending test product notification: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to send test notification: " + e.getMessage()));
        }
    }

    /**
     * Send test user registration notification
     */
    @PostMapping("/test-user-notification")
    public ResponseEntity<ApiResponse<String>> sendTestUserNotification() {
        log.info("Sending test user notification");
        
        CompletableFuture<Boolean> result = telegramService.sendUserRegisteredNotification(
                "test@cambodia-emenu.com",
                "Test User - Sophea Chan",
                "BUSINESS_USER",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
        
        try {
            boolean sent = result.get();
            String message = sent ? "Test user notification sent successfully" : "Failed to send test user notification";
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("Error sending test user notification: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to send test notification: " + e.getMessage()));
        }
    }

    /**
     * Send test business registration notification
     */
    @PostMapping("/test-business-notification")
    public ResponseEntity<ApiResponse<String>> sendTestBusinessNotification() {
        log.info("Sending test business notification");
        
        CompletableFuture<Boolean> result = telegramService.sendBusinessRegisteredNotification(
                "Test Restaurant - Angkor Traditional Kitchen 🏛️",
                "PHAT_MENGHOR",
                "business@cambodia-emenu.com",
                "+855 70 411 260",
                "angkor-kitchen",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
        
        try {
            boolean sent = result.get();
            String message = sent ? "Test business notification sent successfully" : "Failed to send test business notification";
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("Error sending test business notification: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to send test notification: " + e.getMessage()));
        }
    }

    /**
     * Send test message with emoji and formatting
     */
    @PostMapping("/test-formatted-message")
    public ResponseEntity<ApiResponse<String>> sendFormattedTestMessage() {
        log.info("Sending formatted test message");
        
        String formattedMessage = """
                🇰🇭 *Cambodia E-Menu Platform Test* 🇰🇭
                
                ✨ *Features Testing:*
                • ✅ Telegram Integration
                • ✅ Markdown Formatting
                • ✅ Emoji Support
                • ✅ Multi-line Messages
                
                📊 *Platform Stats:*
                • 🏪 Restaurants: `1,250+`
                • 🍽️ Products: `15,000+`
                • 👥 Users: `5,500+`
                
                🔗 *Links:*
                [Documentation](https://docs.cambodia-emenu.com)
                [Support](mailto:support@cambodia-emenu.com)
                
                ---
                🕐 *Time:* %s
                🤖 *System:* Automated Test Message
                """.formatted(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        // ✅ FIXED: Use the correct method signature
        CompletableFuture<Boolean> result = telegramService.sendDirectMessageToUser(
                "1898032377", // Use the configured chat ID
                formattedMessage, 
                "Formatted Test"
        );
        
        try {
            boolean sent = result.get();
            String message = sent ? "Formatted test message sent successfully" : "Failed to send formatted test message";
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("Error sending formatted test message: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to send formatted test message: " + e.getMessage()));
        }
    }

    /**
     * Send broadcast message to all platform users
     */
    @PostMapping("/broadcast-test")
    public ResponseEntity<ApiResponse<String>> sendBroadcastTest(@RequestBody TestMessageRequest request) {
        log.info("Sending broadcast test message");
        
        String broadcastMessage = String.format("""
                📢 <b>Broadcast Test Message</b>
                
                %s
                
                📅 <b>Sent at:</b> %s
                🤖 <b>From:</b> Cambodia E-Menu Platform
                👥 <b>Audience:</b> All Platform Users
                """, 
                request.getMessage(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        // Use the direct message method to admin for testing
        CompletableFuture<Boolean> result = telegramService.sendDirectMessageToUser(
                "1898032377", // Admin chat ID
                broadcastMessage, 
                "Broadcast Test"
        );
        
        try {
            boolean sent = result.get();
            String message = sent ? "Broadcast test message sent successfully" : "Failed to send broadcast test message";
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("Error sending broadcast test message: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to send broadcast test message: " + e.getMessage()));
        }
    }

    /**
     * Send direct message to specific chat ID
     */
    @PostMapping("/send-direct")
    public ResponseEntity<ApiResponse<String>> sendDirectMessage(
            @RequestParam String chatId,
            @RequestBody TestMessageRequest request) {
        log.info("Sending direct message to chat ID: {}", chatId);
        
        String directMessage = String.format("""
                💬 <b>Direct Message</b>
                
                %s
                
                📅 <b>Sent at:</b> %s
                🤖 <b>From:</b> Cambodia E-Menu Platform API
                """, 
                request.getMessage(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        CompletableFuture<Boolean> result = telegramService.sendDirectMessageToUser(
                chatId, 
                directMessage, 
                "Direct Message"
        );
        
        try {
            boolean sent = result.get();
            String message = sent ? "Direct message sent successfully" : "Failed to send direct message";
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("Error sending direct message: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to send direct message: " + e.getMessage()));
        }
    }

    /**
     * Test all notification types
     */
    @PostMapping("/test-all-notifications")
    public ResponseEntity<ApiResponse<String>> testAllNotifications() {
        log.info("Testing all notification types");
        
        try {
            // Test user registration notification
            telegramService.sendUserRegisteredNotification(
                    "test_user_" + System.currentTimeMillis(),
                    "Test User - Complete Testing",
                    "CUSTOMER",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );
            
            // Test business registration notification
            telegramService.sendBusinessRegisteredNotification(
                    "Test Restaurant - Complete Testing",
                    "TEST_OWNER",
                    "test@cambodia-emenu.com",
                    "070 411 260",
                    "test-restaurant-" + System.currentTimeMillis(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );
            
            // Test product creation notification
            telegramService.sendProductCreatedNotification(
                    "Test Product - Complete Testing",
                    "Test Restaurant",
                    "25.00",
                    "Test Category",
                    "TEST_USER",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                    "All notification tests initiated successfully", 
                    "Check your Telegram for notifications"));
        } catch (Exception e) {
            log.error("Error testing all notifications: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Failed to test notifications: " + e.getMessage()));
        }
    }

    /**
     * Request DTO for test messages
     */
    @Setter
    @Getter
    public static class TestMessageRequest {
        private String message;

    }
}