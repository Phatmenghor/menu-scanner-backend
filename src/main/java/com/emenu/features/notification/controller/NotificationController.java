package com.emenu.features.notification.controller;

import com.emenu.features.notification.dto.request.MultiRecipientNotificationRequest;
import com.emenu.features.notification.dto.response.NotificationSendResult;
import com.emenu.features.notification.service.TelegramService;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final TelegramService telegramService;

    // ===== MULTI-RECIPIENT NOTIFICATIONS =====

    /**
     * Send notification to multiple recipients
     */
    @PostMapping("/send-multi")
    public ResponseEntity<ApiResponse<NotificationSendResult>> sendMultiRecipientNotification(
            @Valid @RequestBody MultiRecipientNotificationRequest request) {
        
        log.info("📢 Sending multi-recipient notification: {}", request.getNotificationType());
        
        try {
            CompletableFuture<NotificationSendResult> resultFuture = telegramService.sendMultiRecipientNotification(request);
            NotificationSendResult result = resultFuture.get();
            
            if (result.getAllSuccessful()) {
                return ResponseEntity.ok(ApiResponse.success("Notification sent successfully", result));
            } else {
                return ResponseEntity.ok(ApiResponse.success("Notification sent with some failures", result));
            }
        } catch (Exception e) {
            log.error("❌ Error sending multi-recipient notification: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send notification: " + e.getMessage()));
        }
    }

    /**
     * Send notification to all platform users
     */
    @PostMapping("/platform-broadcast")
    public ResponseEntity<ApiResponse<NotificationSendResult>> sendPlatformBroadcast(
            @RequestBody Map<String, String> request) {
        
        String message = request.get("message");
        String title = request.getOrDefault("title", "Platform Announcement");
        
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("Message is required"));
        }
        
        log.info("📢 Sending platform broadcast: {}", title);
        
        try {
            MultiRecipientNotificationRequest notificationRequest = MultiRecipientNotificationRequest.builder()
                    .notificationType("PLATFORM_BROADCAST")
                    .title(title)
                    .message(String.format("""
                            📢 <b>%s</b>
                            
                            %s
                            
                            📅 <b>Sent:</b> %s
                            🤖 <b>From:</b> Cambodia E-Menu Platform
                            """, 
                            title, 
                            message,
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))))
                    .includePlatformUsers(true)
                    .includeBusinessOwners(false)
                    .includeCustomers(false)
                    .sendImmediate(true)
                    .sourceAction("PLATFORM_BROADCAST")
                    .build();
            
            CompletableFuture<NotificationSendResult> resultFuture = telegramService.sendMultiRecipientNotification(notificationRequest);
            NotificationSendResult result = resultFuture.get();
            
            return ResponseEntity.ok(ApiResponse.success("Platform broadcast completed", result));
        } catch (Exception e) {
            log.error("❌ Error sending platform broadcast: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send broadcast: " + e.getMessage()));
        }
    }

    /**
     * Send notification to all business owners
     */
    @PostMapping("/business-broadcast")
    public ResponseEntity<ApiResponse<NotificationSendResult>> sendBusinessBroadcast(
            @RequestBody Map<String, String> request) {
        
        String message = request.get("message");
        String title = request.getOrDefault("title", "Business Announcement");
        
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("Message is required"));
        }
        
        log.info("🏪 Sending business broadcast: {}", title);
        
        try {
            MultiRecipientNotificationRequest notificationRequest = MultiRecipientNotificationRequest.builder()
                    .notificationType("BUSINESS_BROADCAST")
                    .title(title)
                    .message(String.format("""
                            🏪 <b>%s</b>
                            
                            %s
                            
                            📅 <b>Sent:</b> %s
                            📞 <b>Support:</b> support@cambodia-emenu.com
                            """, 
                            title, 
                            message,
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))))
                    .includePlatformUsers(false)
                    .includeBusinessOwners(true)
                    .includeCustomers(false)
                    .sendImmediate(true)
                    .sourceAction("BUSINESS_BROADCAST")
                    .build();
            
            CompletableFuture<NotificationSendResult> resultFuture = telegramService.sendMultiRecipientNotification(notificationRequest);
            NotificationSendResult result = resultFuture.get();
            
            return ResponseEntity.ok(ApiResponse.success("Business broadcast completed", result));
        } catch (Exception e) {
            log.error("❌ Error sending business broadcast: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send broadcast: " + e.getMessage()));
        }
    }

    /**
     * Send notification to all customers
     */
    @PostMapping("/customer-broadcast")
    public ResponseEntity<ApiResponse<NotificationSendResult>> sendCustomerBroadcast(
            @RequestBody Map<String, String> request) {
        
        String message = request.get("message");
        String title = request.getOrDefault("title", "Customer Announcement");
        
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("Message is required"));
        }
        
        log.info("👥 Sending customer broadcast: {}", title);
        
        try {
            MultiRecipientNotificationRequest notificationRequest = MultiRecipientNotificationRequest.builder()
                    .notificationType("CUSTOMER_BROADCAST")
                    .title(title)
                    .message(String.format("""
                            👥 <b>%s</b>
                            
                            %s
                            
                            📅 <b>Sent:</b> %s
                            🍽️ <b>Enjoy:</b> Explore restaurants on our platform!
                            """, 
                            title, 
                            message,
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))))
                    .includePlatformUsers(false)
                    .includeBusinessOwners(false)
                    .includeCustomers(true)
                    .sendImmediate(true)
                    .sourceAction("CUSTOMER_BROADCAST")
                    .build();
            
            CompletableFuture<NotificationSendResult> resultFuture = telegramService.sendMultiRecipientNotification(notificationRequest);
            NotificationSendResult result = resultFuture.get();
            
            return ResponseEntity.ok(ApiResponse.success("Customer broadcast completed", result));
        } catch (Exception e) {
            log.error("❌ Error sending customer broadcast: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send broadcast: " + e.getMessage()));
        }
    }

    // ===== NOTIFICATION TESTING =====

    /**
     * Test user registration notification
     */
    @PostMapping("/test/user-registration")
    public ResponseEntity<ApiResponse<String>> testUserRegistrationNotification() {
        log.info("🧪 Testing user registration notification");
        
        try {
            CompletableFuture<Boolean> result = telegramService.sendUserRegisteredNotification(
                    "test_user_" + System.currentTimeMillis(),
                    "Test User (Generated)",
                    "CUSTOMER",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );
            
            boolean sent = result.get();
            String message = sent ? "User registration notification test sent successfully" : "User registration notification test failed";
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("❌ Error in user registration notification test: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Test failed: " + e.getMessage()));
        }
    }

    /**
     * Test business registration notification
     */
    @PostMapping("/test/business-registration")
    public ResponseEntity<ApiResponse<String>> testBusinessRegistrationNotification() {
        log.info("🧪 Testing business registration notification");
        
        try {
            long timestamp = System.currentTimeMillis();
            CompletableFuture<Boolean> result = telegramService.sendBusinessRegisteredNotification(
                    "Test Restaurant " + timestamp,
                    "Test Owner",
                    "test@cambodia-emenu.com",
                    "070 411 260",
                    "test-restaurant-" + timestamp,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );
            
            boolean sent = result.get();
            String message = sent ? "Business registration notification test sent successfully" : "Business registration notification test failed";
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("❌ Error in business registration notification test: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Test failed: " + e.getMessage()));
        }
    }

    /**
     * Test product creation notification
     */
    @PostMapping("/test/product-creation")
    public ResponseEntity<ApiResponse<String>> testProductCreationNotification() {
        log.info("🧪 Testing product creation notification");
        
        try {
            CompletableFuture<Boolean> result = telegramService.sendProductCreatedNotification(
                    "Test Product - Delicious Amok Fish 🐟",
                    "Test Restaurant - Phat's Kitchen 🍽️",
                    "15.50",
                    "Traditional Khmer Cuisine",
                    "TEST_USER",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );
            
            boolean sent = result.get();
            String message = sent ? "Product creation notification test sent successfully" : "Product creation notification test failed";
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("❌ Error in product creation notification test: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Test failed: " + e.getMessage()));
        }
    }

    // ===== NOTIFICATION STATUS & MANAGEMENT =====

    /**
     * Get notification settings for current user
     */
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotificationSettings() {
        // This would typically get user's notification preferences
        // For now, return basic Telegram status
        
        try {
            // Get current user Telegram status
            boolean hasTelegram = false;
            boolean canReceiveNotifications = false;
            
            // In a real implementation, you'd get this from SecurityUtils
            // hasTelegram = securityUtils.currentUserHasTelegram();
            // canReceiveNotifications = securityUtils.currentUserCanReceiveTelegramNotifications();
            
            Map<String, Object> settings = Map.of(
                    "hasTelegramLinked", hasTelegram,
                    "canReceiveTelegramNotifications", canReceiveNotifications,
                    "supportedChannels", List.of("telegram", "email"),
                    "defaultChannel", "telegram"
            );
            
            return ResponseEntity.ok(ApiResponse.success("Notification settings retrieved", settings));
        } catch (Exception e) {
            log.error("❌ Error getting notification settings: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Failed to get notification settings: " + e.getMessage()));
        }
    }

    /**
     * Test connection to notification services
     */
    @GetMapping("/test-connection")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testNotificationConnections() {
        log.info("🔗 Testing notification service connections");
        
        try {
            boolean telegramConnected = telegramService.testConnection();
            
            Map<String, Object> status = Map.of(
                    "telegram", Map.of(
                            "connected", telegramConnected,
                            "service", "Telegram Bot API",
                            "status", telegramConnected ? "Healthy" : "Unavailable"
                    ),
                    "email", Map.of(
                            "connected", false, // Placeholder
                            "service", "SMTP",
                            "status", "Not implemented"
                    ),
                    "overall", Map.of(
                            "healthy", telegramConnected,
                            "availableChannels", telegramConnected ? List.of("telegram") : List.of(),
                            "testedAt", LocalDateTime.now()
                    )
            );
            
            return ResponseEntity.ok(ApiResponse.success("Connection test completed", status));
        } catch (Exception e) {
            log.error("❌ Error testing notification connections: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Connection test failed: " + e.getMessage()));
        }
    }

    /**
     * Send welcome notification to new user
     */
    @PostMapping("/welcome/{userId}")
    public ResponseEntity<ApiResponse<String>> sendWelcomeNotification(@PathVariable String userId) {
        log.info("🎉 Sending welcome notification to user: {}", userId);
        
        try {
            // In a real implementation, you'd get the user and send welcome notification
            // User user = userService.getUserById(UUID.fromString(userId));
            // userService.sendWelcomeNotification(user);
            
            String message = "Welcome notification functionality is implemented in UserService";
            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (Exception e) {
            log.error("❌ Error sending welcome notification: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Failed to send welcome notification: " + e.getMessage()));
        }
    }
}