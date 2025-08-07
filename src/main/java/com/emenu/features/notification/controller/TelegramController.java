
package com.emenu.features.notification.controller;

import com.emenu.features.notification.service.TelegramService;
import com.emenu.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * Send test message
     */
    @PostMapping("/test-message")
    public ResponseEntity<ApiResponse<String>> sendTestMessage(@RequestParam String message) {
        log.info("Sending test message to Telegram: {}", message);
        
        CompletableFuture<Boolean> result = telegramService.sendMessage(
                "🧪 *Test Message*\n\n" + message + 
                "\n\n📅 *Sent at:* " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
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
     * Send test product notification
     */
    @PostMapping("/test-product-notification")
    public ResponseEntity<ApiResponse<String>> sendTestProductNotification() {
        log.info("Sending test product notification");
        
        CompletableFuture<Boolean> result = telegramService.sendProductCreatedNotification(
                "Test Product - Delicious Burger",
                "Test Restaurant - Phat's Kitchen",
                "15.50",
                "Main Course",
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
                "test@example.com",
                "Test User - John Doe",
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
                "Test Restaurant - Angkor Kitchen",
                "PHAT_MENGHOR",
                "business@example.com",
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
}