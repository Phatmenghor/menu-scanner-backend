package com.emenu.features.notification.dto.filter;

import com.emenu.enums.notification.MessageType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageThreadFilterRequest {
    
    private String subject;
    private MessageType messageType;
    private UUID businessId;
    private UUID customerId;
    private UUID platformUserId;
    private Boolean isSystemGenerated;
    private Integer priority;
    private Boolean isClosed;
    private LocalDateTime lastMessageAfter;
    private LocalDateTime lastMessageBefore;
    private String search;
    
    // Pagination
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String sortBy = "lastMessageAt";
    private String sortDirection = "DESC";
}