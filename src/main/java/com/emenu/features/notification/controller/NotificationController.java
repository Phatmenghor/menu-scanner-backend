package com.emenu.features.notification.controller;

import com.emenu.features.notification.dto.filter.NotificationFilterRequest;
import com.emenu.features.notification.dto.request.BulkNotificationRequest;
import com.emenu.features.notification.dto.request.NotificationCreateRequest;
import com.emenu.features.notification.dto.response.NotificationResponse;
import com.emenu.features.notification.service.NotificationService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Create a single notification
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT')")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(@Valid @RequestBody NotificationCreateRequest request) {
        log.info("Creating notification for recipient: {}", request.getRecipientId());
        NotificationResponse notification = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification created successfully", notification));
    }

    /**
     * Schedule a notification
     */
    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT')")
    public ResponseEntity<ApiResponse<NotificationResponse>> scheduleNotification(@Valid @RequestBody NotificationCreateRequest request) {
        log.info("Scheduling notification for: {}", request.getScheduledAt());
        NotificationResponse notification = notificationService.scheduleNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification scheduled successfully", notification));
    }

    /**
     * Create bulk notifications
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> createBulkNotification(@Valid @RequestBody BulkNotificationRequest request) {
        log.info("Creating bulk notification");
        List<NotificationResponse> notifications = notificationService.createBulkNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk notifications created successfully", notifications));
    }

    /**
     * Send a specific notification
     */
    @PostMapping("/{notificationId}/send")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT')")
    public ResponseEntity<ApiResponse<Void>> sendNotification(@PathVariable UUID notificationId) {
        log.info("Sending notification: {}", notificationId);
        notificationService.sendNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification sent successfully", null));
    }

    /**
     * Send pending notifications
     */
    @PostMapping("/send-pending")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendPendingNotifications() {
        log.info("Sending pending notifications");
        notificationService.sendPendingNotifications();
        return ResponseEntity.ok(ApiResponse.success("Pending notifications sent successfully", null));
    }

    /**
     * Get notifications with filtering and pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT')")
    public ResponseEntity<ApiResponse<PaginationResponse<NotificationResponse>>> getNotifications(@ModelAttribute NotificationFilterRequest filter) {
        log.info("Getting notifications with filter");
        PaginationResponse<NotificationResponse> notifications = notificationService.getNotifications(filter);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    /**
     * Get a specific notification
     */
    @GetMapping("/{notificationId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(@PathVariable UUID notificationId) {
        log.info("Getting notification by ID: {}", notificationId);
        NotificationResponse notification = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", notification));
    }

    /**
     * Get current user's notifications
     */
    @GetMapping("/my-notifications")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        log.info("Getting current user's notifications, unread only: {}", unreadOnly);
        List<NotificationResponse> notifications = notificationService.getUserNotifications(UUID.randomUUID(), unreadOnly); // Placeholder
        return ResponseEntity.ok(ApiResponse.success("User notifications retrieved successfully", notifications));
    }

    /**
     * Get current user's unread notification count
     */
    @GetMapping("/my-unread-count")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Long>> getMyUnreadCount() {
        log.info("Getting current user's unread notification count");
        long count = notificationService.getUnreadNotificationCount(UUID.randomUUID()); // Placeholder
        return ResponseEntity.ok(ApiResponse.success("Unread notification count retrieved successfully", count));
    }

    /**
     * Mark a notification as read
     */
    @PostMapping("/{notificationId}/mark-read")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID notificationId) {
        log.info("Marking notification as read: {}", notificationId);
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    /**
     * Mark all notifications as read for current user
     */
    @PostMapping("/mark-all-read")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        log.info("Marking all notifications as read for current user");
        notificationService.markAllAsRead(UUID.randomUUID()); // Placeholder
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID notificationId) {
        log.info("Deleting notification: {}", notificationId);
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }

    /**
     * Retry failed notifications
     */
    @PostMapping("/retry-failed")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> retryFailedNotifications() {
        log.info("Retrying failed notifications");
        notificationService.retryFailedNotifications();
        return ResponseEntity.ok(ApiResponse.success("Failed notifications retried successfully", null));
    }

    /**
     * Get failed notifications
     */
    @GetMapping("/failed")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getFailedNotifications() {
        log.info("Getting failed notifications");
        List<NotificationResponse> notifications = notificationService.getFailedNotifications();
        return ResponseEntity.ok(ApiResponse.success("Failed notifications retrieved successfully", notifications));
    }
}
