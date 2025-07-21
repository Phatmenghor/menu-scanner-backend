package com.emenu.features.notification.dto.response;

import com.emenu.enums.notification.NotificationChannel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CommunicationHistoryResponse {
    
    private UUID id;
    private UUID recipientId;
    private String recipientName;
    private UUID senderId;
    private String senderName;
    private UUID businessId;
    private String businessName;
    private NotificationChannel channel;
    private String subject;
    private String content;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private String errorMessage;
    private String externalMessageId;
    private UUID relatedThreadId;
    private UUID relatedMessageId;
    private LocalDateTime createdAt;
}