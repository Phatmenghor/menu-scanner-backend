package com.emenu.features.messaging.dto.response;

import com.emenu.enums.MessageStatus;
import com.emenu.enums.MessageType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageSummaryResponse {
    
    private UUID id;
    private String senderName;
    private String recipientName;
    private String subject;
    private MessageType messageType;
    private MessageStatus status;
    private String priority;
    private LocalDateTime createdAt;
    private Boolean isRead;
}