package com.emenu.features.messaging.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BroadcastMessageRequest {
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private MessageType messageType = MessageType.ANNOUNCEMENT;
    private String priority = "NORMAL";
    
    // Target specific users
    private List<UUID> userIds;
    
    // Target user types
    private List<UserType> userTypes;
    
    // Target businesses
    private List<UUID> businessIds;
    
    // Send to all
    private Boolean sendToAll = false;
}