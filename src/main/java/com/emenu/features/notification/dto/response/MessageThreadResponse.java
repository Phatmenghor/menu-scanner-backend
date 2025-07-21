package com.emenu.features.notification.dto.response;

import com.emenu.enums.notification.MessageType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class MessageThreadResponse {
    
    private UUID id;
    private String subject;
    private MessageType messageType;
    private List<UUID> participantIds;
    private UUID businessId;
    private String businessName;
    private UUID customerId;
    private String customerName;
    private UUID platformUserId;
    private String platformUserName;
    private Boolean isSystemGenerated;
    private Integer priority;
    private String priorityDisplay;
    private Boolean isClosed;
    private LocalDateTime closedAt;
    private UUID closedBy;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
    private Integer messageCount;
    private MessageResponse lastMessage;
    private List<MessageResponse> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}