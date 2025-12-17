package com.emenu.features.notification.service;

import com.emenu.features.notification.dto.filter.NotificationFilterRequest;
import com.emenu.features.notification.dto.request.NotificationRequest;
import com.emenu.features.notification.dto.resposne.NotificationResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationResponse> sendNotification(NotificationRequest request);
    
    NotificationResponse getNotificationById(UUID notificationId);
    
    PaginationResponse<NotificationResponse> getMyNotifications(NotificationFilterRequest request);
    
    PaginationResponse<NotificationResponse> getAllNotifications(NotificationFilterRequest request);

    long getUnreadCount();

    long getUnseenCount();

    int markAllAsSeen();

    NotificationResponse markAsRead(UUID notificationId);

    int markAllAsRead();

    void deleteNotification(UUID notificationId);

    int deleteAllNotifications();
}