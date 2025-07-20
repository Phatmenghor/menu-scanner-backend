package com.emenu.features.messaging.dto.response;

import com.emenu.enums.MessageStatus;
import com.emenu.enums.MessageType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageResponse {
    
    private UUID id;
    private UUID senderId;
    private String senderEmail;
    private String senderName;
    private UUID recipientId;
    private String recipientEmail;
    private String recipientName;
    private String subject;
    private String content;
    private MessageType messageType;
    private MessageStatus status;
    private String priority;
    private UUID businessId;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isRead;
}
