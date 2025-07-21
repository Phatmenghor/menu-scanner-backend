package com.emenu.features.notification.service;

import com.emenu.enums.notification.NotificationChannel;
import com.emenu.features.notification.dto.filter.NotificationFilterRequest;
import com.emenu.features.notification.dto.request.BulkNotificationRequest;
import com.emenu.features.notification.dto.request.NotificationCreateRequest;
import com.emenu.features.notification.dto.response.NotificationResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface NotificationService {
    
    // Single Notification Management
    NotificationResponse createNotification(NotificationCreateRequest request);
    NotificationResponse scheduleNotification(NotificationCreateRequest request);
    void sendNotification(UUID notificationId);
    void sendPendingNotifications();
    
    // Bulk Notifications
    List<NotificationResponse> createBulkNotification(BulkNotificationRequest request);
    void sendBulkNotifications(List<UUID> notificationIds);
    
    // Notification Queries
    PaginationResponse<NotificationResponse> getNotifications(NotificationFilterRequest filter);
    NotificationResponse getNotificationById(UUID notificationId);
    List<NotificationResponse> getUserNotifications(UUID userId, boolean unreadOnly);
    long getUnreadNotificationCount(UUID userId);
    
    // Notification Actions
    void markAsRead(UUID notificationId);
    void markAllAsRead(UUID userId);
    void deleteNotification(UUID notificationId);
    
    // Template-based Notifications
    NotificationResponse sendTemplatedNotification(UUID recipientId, String templateName,
                                                   Map<String, String> variables, NotificationChannel channel);
    
    // System Alerts
    void sendSubscriptionExpiryAlert(UUID businessId, int daysRemaining);
    void sendSubscriptionExpiredAlert(UUID businessId);
    void sendPaymentReminderAlert(UUID businessId);
    void sendWelcomeNotification(UUID userId);
    void sendPasswordResetNotification(UUID userId, String resetToken);
    
    // Retry and Error Handling
    void retryFailedNotifications();
    List<NotificationResponse> getFailedNotifications();
}
