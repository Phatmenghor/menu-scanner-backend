package com.emenu.features.auth.dto.request;

import com.emenu.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CustomerMessageRequest {
    
    private UUID recipientId; // If sending to specific business/staff
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private MessageType messageType = MessageType.GENERAL;
    private String priority = "NORMAL";
}
