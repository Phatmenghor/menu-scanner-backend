package com.emenu.features.notification.service;

import com.emenu.features.notification.dto.filter.NotificationFilterRequest;
import com.emenu.features.notification.dto.request.NotificationRequest;
import com.emenu.features.notification.dto.resposne.NotificationResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    // ===== CREATE =====
    /**
     * Send notification(s) - automatically handles individual or group based on recipientType
     */
    List<NotificationResponse> sendNotification(NotificationRequest request);
    
    // ===== READ =====
    NotificationResponse getNotificationById(UUID notificationId);
    
    PaginationResponse<NotificationResponse> getMyNotifications(NotificationFilterRequest request);
    
    PaginationResponse<NotificationResponse> getAllNotifications(NotificationFilterRequest request);
    
    long getUnreadCount();
    
    // ===== UPDATE =====
    NotificationResponse markAsRead(UUID notificationId);
    
    void markAllAsRead();
    
    int markGroupAsRead(UUID groupId);
    
    // ===== DELETE =====
    void deleteNotification(UUID notificationId);
    
    void deleteAllReadNotifications();
    
    int deleteGroupNotifications(UUID groupId);
}