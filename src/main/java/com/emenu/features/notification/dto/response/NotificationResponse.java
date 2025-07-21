package com.emenu.features.notification.dto.response;

import com.emenu.enums.notification.AlertType;
import com.emenu.enums.notification.NotificationChannel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationResponse {
    
    private UUID id;
    private UUID recipientId;
    private String recipientName;
    private UUID senderId;
    private String senderName;
    private String title;
    private String content;
    private String htmlContent;
    private AlertType alertType;
    private NotificationChannel channel;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Boolean isSent;
    private LocalDateTime sentAt;
    private String deliveryStatus;
    private String errorMessage;
    private LocalDateTime scheduledAt;
    private UUID businessId;
    private String businessName;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}