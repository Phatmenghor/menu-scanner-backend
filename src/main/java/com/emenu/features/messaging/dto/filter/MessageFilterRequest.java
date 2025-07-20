package com.emenu.features.messaging.dto.filter;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageFilterRequest {
    
    private UUID senderId;
    private UUID recipientId;
    private String senderEmail;
    private String recipientEmail;
    private String subject;
    private String content;
    private MessageType messageType;
    private MessageStatus status;
    private String priority;
    private UUID businessId;
    private Boolean isRead;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private String search;
    
    // Pagination
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}