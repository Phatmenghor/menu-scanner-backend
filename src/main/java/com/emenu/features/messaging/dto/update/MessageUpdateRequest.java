package com.emenu.features.messaging.dto.update;

import lombok.Data;

@Data
public class MessageUpdateRequest {
    
    private String subject;
    private String content;
    private String priority;
    private MessageStatus status;
}