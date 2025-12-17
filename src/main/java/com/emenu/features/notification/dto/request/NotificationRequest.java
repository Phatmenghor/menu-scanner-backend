package com.emenu.features.notification.dto.request;

import com.emenu.enums.notification.MessageType;
import com.emenu.enums.notification.NotificationPriority;
import com.emenu.enums.notification.NotificationRecipientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class NotificationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Message type is required")
    private MessageType messageType;

    @NotNull(message = "Recipient type is required")
    private NotificationRecipientType recipientType;

    private NotificationPriority priority = NotificationPriority.NORMAL;

    // For INDIVIDUAL_USER notifications
    private UUID userId;
    private String userName;

    // For BUSINESS_TEAM_GROUP notifications
    private UUID businessId;

    // Send copy to system owners for monitoring
    private Boolean sendSystemCopy = false;
}