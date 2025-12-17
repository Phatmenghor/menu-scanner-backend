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

    // ===== CREATE =====
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

    @GetMapping("my/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getMyNotification(
            @PathVariable UUID notificationId) {
        log.info("Get my notification: {}", notificationId);
        NotificationResponse response = notificationService.getMyNotificationById(notificationId);
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
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
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

    @GetMapping("/unseen-count")
    public ResponseEntity<ApiResponse<Long>> getUnseenCount() {
        log.info("Get unseen count (for badge)");
        long count = notificationService.getUnseenCount();
        return ResponseEntity.ok(ApiResponse.success("Unseen count", count));
    }

    // ===== UPDATE (Only 3 methods - self notifications only) =====
    
    @PutMapping("/mark-all-seen")
    public ResponseEntity<ApiResponse<Integer>> markAllAsSeen() {
        log.info("Mark all as seen (clear badge)");
        int updated = notificationService.markAllAsSeen();
        return ResponseEntity.ok(ApiResponse.success("Badge cleared", updated));
    }
    
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable UUID notificationId) {
        log.info("Mark as read: {}", notificationId);
        NotificationResponse response = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", response));
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead() {
        log.info("Mark all as read");
        int updated = notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All marked as read", updated));
    }

    // ===== DELETE (Only 2 methods - self notifications only) =====
    
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> deleteNotification(
            @PathVariable UUID notificationId) {
        log.info("Delete notification: {}", notificationId);
        NotificationResponse response = notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", response));
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponse<Integer>> deleteAllNotifications() {
        log.info("Delete all my notifications");
        int deleted = notificationService.deleteAllNotifications();
        return ResponseEntity.ok(ApiResponse.success("All notifications deleted", deleted));
    }
}