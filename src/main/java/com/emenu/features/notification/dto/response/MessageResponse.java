package com.emenu.features.notification.dto.response;

import com.emenu.enums.notification.MessageStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class MessageResponse {
    
    private UUID id;
    private UUID threadId;
    private UUID senderId;
    private String senderName;
    private String senderEmail;
    private String content;
    private String htmlContent;
    private MessageStatus status;
    private Boolean isSystemMessage;
    private UUID parentMessageId;
    private List<MessageResponse> replies;
    private LocalDateTime readAt;
    private LocalDateTime deliveredAt;
    private List<MessageAttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}