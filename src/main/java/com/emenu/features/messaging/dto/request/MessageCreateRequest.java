package com.emenu.features.messaging.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageCreateRequest {
    
    @NotNull(message = "Recipient ID is required")
    private UUID recipientId;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private MessageType messageType = MessageType.GENERAL;
    private String priority = "NORMAL";
    private UUID businessId;
}