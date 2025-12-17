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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        log.info("Sending notification - Recipient: {}", request.getRecipientType());
        List<NotificationResponse> responses = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification sent", responses));
    }

    // ===== READ =====
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
            @PathVariable UUID notificationId) {
        log.info("Get notification: {}", notificationId);
        NotificationResponse response = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved", response));
    }

    @PostMapping("/my")
    public ResponseEntity<ApiResponse<PaginationResponse<NotificationResponse>>> getMyNotifications(
            @Valid @RequestBody NotificationFilterRequest request) {
        log.info("Get my notifications");
        PaginationResponse<NotificationResponse> response = notificationService.getMyNotifications(request);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", response));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<NotificationResponse>>> getAllNotifications(
            @Valid @RequestBody NotificationFilterRequest request) {
        log.info("Get all notifications");
        PaginationResponse<NotificationResponse> response = notificationService.getAllNotifications(request);
        return ResponseEntity.ok(ApiResponse.success("All notifications retrieved", response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        log.info("Get unread count");
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success("Unread count", count));
    }

    // ===== UPDATE =====
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable UUID notificationId) {
        log.info("Mark as read: {}", notificationId);
        NotificationResponse response = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", response));
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        log.info("Mark all as read");
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All marked as read", null));
    }

    @PutMapping("/group/{groupId}/read")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<Integer>> markGroupAsRead(@PathVariable UUID groupId) {
        log.info("Mark group as read: {}", groupId);
        int updated = notificationService.markGroupAsRead(groupId);
        return ResponseEntity.ok(ApiResponse.success("Group marked as read", updated));
    }

    // ===== DELETE =====
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID notificationId) {
        log.info("Delete notification: {}", notificationId);
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }

    @DeleteMapping("/delete-read")
    public ResponseEntity<ApiResponse<Void>> deleteAllReadNotifications() {
        log.info("Delete all read notifications");
        notificationService.deleteAllReadNotifications();
        return ResponseEntity.ok(ApiResponse.success("Read notifications deleted", null));
    }

    @DeleteMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<Integer>> deleteGroupNotifications(@PathVariable UUID groupId) {
        log.info("Delete group notifications: {}", groupId);
        int deleted = notificationService.deleteGroupNotifications(groupId);
        return ResponseEntity.ok(ApiResponse.success("Group notifications deleted", deleted));
    }
}