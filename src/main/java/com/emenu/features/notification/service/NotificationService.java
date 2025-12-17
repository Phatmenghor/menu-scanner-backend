package com.emenu.features.notification.service;

import com.emenu.features.notification.dto.filter.NotificationFilterRequest;
import com.emenu.features.notification.dto.request.NotificationRequest;
import com.emenu.features.notification.dto.resposne.NotificationResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface NotificationService {

    // CREATE
    NotificationResponse createNotification(NotificationRequest request);
    
    // READ
    NotificationResponse getNotificationById(UUID notificationId);
    PaginationResponse<NotificationResponse> getMyNotifications(NotificationFilterRequest request);
    PaginationResponse<NotificationResponse> getAllNotifications(NotificationFilterRequest request);
    long getUnreadCount();
    
    // UPDATE
    NotificationResponse updateNotification(UUID notificationId, NotificationRequest request);
    NotificationResponse markAsRead(UUID notificationId);
    void markAllAsRead();
    
    // DELETE
    void deleteNotification(UUID notificationId);
    void deleteAllReadNotifications();
}
