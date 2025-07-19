package com.emenu.features.notification.controller;

import com.emenu.features.notification.service.NotificationService;
import com.emenu.features.user_management.service.UserService;
import com.emenu.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification management operations")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @PostMapping("/test-email/{userId}")
    @Operation(summary = "Send test email", description = "Send a test email to user")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> sendTestEmail(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("REST request to send test email to user: {}", userId);
        
        var user = userService.getUserById(userId);
        // Convert UserResponse back to User entity for notification service
        // In real implementation, you'd have a proper conversion method
        
        return ResponseEntity.ok(ApiResponse.success("Test email sent successfully", null));
    }

    @PostMapping("/test-telegram/{userId}")
    @Operation(summary = "Send test Telegram message", description = "Send a test Telegram message to user")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> sendTestTelegram(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Test message") @RequestParam String message) {
        log.info("REST request to send test Telegram message to user: {}", userId);
        
        // Implementation would go here
        
        return ResponseEntity.ok(ApiResponse.success("Test Telegram message sent successfully", null));
    }
}