package com.emenu.features.auth.dto.request;

import com.emenu.enums.MessageType;
import com.emenu.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PlatformMessageRequest {
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private MessageType messageType = MessageType.ANNOUNCEMENT;
    private String priority = "NORMAL";
    
    // Send to specific users
    private List<UUID> userIds;
    
    // Or send to user types
    private List<UserType> userTypes;
    
    // Or send to all users in businesses
    private List<UUID> businessIds;
    
    // Or send to all users
    private Boolean sendToAll = false;
}