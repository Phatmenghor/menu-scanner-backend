package com.emenu.features.messaging.dto.request;

import com.emenu.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class MessageSendRequest {
    
    @NotBlank(message = "Message template key is required")
    private String templateKey;
    
    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;
    
    // Send to specific users
    private List<UUID> userIds;
    
    // Or send to user types
    private List<UserType> userTypes;
    
    // Template variables
    private Map<String, Object> variables;
    
    // Override default subject/body
    private String customSubject;
    private String customBody;
}
