package com.emenu.features.notification.dto.request;

import com.emenu.enums.notification.AlertType;
import com.emenu.enums.notification.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class NotificationCreateRequest {
    
    @NotNull(message = "Recipient ID is required")
    private UUID recipientId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String htmlContent;
    
    @NotNull(message = "Channel is required")
    private NotificationChannel channel;
    
    private AlertType alertType;
    private UUID businessId;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private LocalDateTime scheduledAt;
    private Map<String, String> metadata;
}