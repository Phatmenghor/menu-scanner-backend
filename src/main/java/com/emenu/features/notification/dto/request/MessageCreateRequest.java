package com.emenu.features.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageCreateRequest {
    
    @NotNull(message = "Thread ID is required")
    private UUID threadId;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String htmlContent;
    private UUID parentMessageId; // For replies
    private Boolean isSystemMessage = false;
}