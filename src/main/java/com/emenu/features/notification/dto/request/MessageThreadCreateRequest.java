package com.emenu.features.notification.dto.request;

import com.emenu.enums.notification.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class MessageThreadCreateRequest {
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotNull(message = "Message type is required")
    private MessageType messageType;
    
    @NotBlank(message = "Initial message content is required")
    private String content;
    
    private String htmlContent;
    
    private List<UUID> participantIds;
    private UUID businessId;
    private UUID customerId;
    private Integer priority = 1;
    private Boolean isSystemGenerated = false;
}
