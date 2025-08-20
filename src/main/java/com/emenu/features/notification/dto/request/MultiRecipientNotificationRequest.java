package com.emenu.features.notification.dto.request;

import com.emenu.enums.user.UserType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class MultiRecipientNotificationRequest {

    private String notificationType;
    private String title;
    private String message;
    private Map<String, Object> templateData;

    // Recipients
    private List<UserType> recipientUserTypes; // Send to all users of these types
    private List<UUID> specificUserIds; // Send to specific users
    private List<Long> telegramUserIds; // Send to specific Telegram users

    // ✅ FIXED: Add @Builder.Default for all fields with default values
    @Builder.Default
    private Boolean includePlatformUsers = false;

    @Builder.Default
    private Boolean includeBusinessOwners = false;

    @Builder.Default
    private Boolean includeCustomers = false;

    // Options
    @Builder.Default
    private Boolean sendToPlatformUsers = true;

    @Builder.Default
    private Boolean sendToTelegramUsers = true;

    @Builder.Default
    private Boolean sendImmediate = true;

    private LocalDateTime scheduleFor; // Future scheduling

    // Metadata
    private String sourceUserId; // Who triggered this notification
    private String sourceAction; // What action triggered it
    private Map<String, String> metadata;
}