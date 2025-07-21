package com.emenu.features.notification.dto.request;

import com.emenu.enums.UserType;
import com.emenu.enums.notification.AlertType;
import com.emenu.enums.notification.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class BulkNotificationRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String htmlContent;
    
    @NotNull(message = "Channel is required")
    private NotificationChannel channel;
    
    private AlertType alertType;
    
    // Target criteria
    private List<UUID> specificUserIds;
    private List<UserType> userTypes;
    private List<UUID> businessIds;
    private Boolean onlyActiveUsers = true;
    
    private LocalDateTime scheduledAt;
    private Map<String, String> metadata;
}