package com.emenu.features.notification.dto.request;

import com.emenu.enums.notification.MessageType;
import com.emenu.enums.notification.NotificationChannel;
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

    private NotificationChannel channel = NotificationChannel.IN_APP;

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String userName;

    private UUID businessId;

    private String referenceType;
    private UUID referenceId;
    private String actionUrl;

    // Flag to also send copy to system owner
    private Boolean sendSystemCopy = false;
}
