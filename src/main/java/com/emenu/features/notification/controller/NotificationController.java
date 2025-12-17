package com.emenu.features.notification.controller;

import com.emenu.features.notification.dto.filter.NotificationFilterRequest;
import com.emenu.features.notification.dto.request.NotificationRequest;
import com.emenu.features.notification.dto.resposne.NotificationResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    // CREATE - Send new notification
    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        log.info("Creating notification");
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification created", response));
    }

    // READ - Get single notification
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(@PathVariable UUID notificationId) {
        log.info("Get notification: {}", notificationId);
        NotificationResponse response = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved", response));
    }

    // READ - Get my notifications
    @PostMapping("/my")
    public ResponseEntity<ApiResponse<PaginationResponse<NotificationResponse>>> getMyNotifications(
            @Valid @RequestBody NotificationFilterRequest request) {
        log.info("Get my notifications");
        PaginationResponse<NotificationResponse> response = notificationService.getMyNotifications(request);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", response));
    }

    // READ - Get all notifications (Admin)
    @PostMapping("/all")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse<NotificationResponse>>> getAllNotifications(
            @Valid @RequestBody NotificationFilterRequest request) {
        log.info("Get all notifications");
        PaginationResponse<NotificationResponse> response = notificationService.getAllNotifications(request);
        return ResponseEntity.ok(ApiResponse.success("All notifications retrieved", response));
    }

    // READ - Get unread count
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        log.info("Get unread count");
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success("Unread count", count));
    }

    // UPDATE - Update notification
    @PutMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> updateNotification(
            @PathVariable UUID notificationId,
            @Valid @RequestBody NotificationRequest request) {
        log.info("Update notification: {}", notificationId);
        NotificationResponse response = notificationService.updateNotification(notificationId, request);
        return ResponseEntity.ok(ApiResponse.success("Notification updated", response));
    }

    // UPDATE - Mark as read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID notificationId) {
        log.info("Mark as read: {}", notificationId);
        NotificationResponse response = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", response));
    }

    // UPDATE - Mark all as read
    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        log.info("Mark all as read");
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All marked as read", null));
    }

    // DELETE - Delete notification
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID notificationId) {
        log.info("Delete notification: {}", notificationId);
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }

    // DELETE - Delete all read notifications
    @DeleteMapping("/delete-read")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAllReadNotifications() {
        log.info("Delete all read notifications");
        notificationService.deleteAllReadNotifications();
        return ResponseEntity.ok(ApiResponse.success("Read notifications deleted", null));
    }
}
