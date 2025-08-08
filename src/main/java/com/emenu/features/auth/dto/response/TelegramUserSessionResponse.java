package com.emenu.features.auth.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class TelegramUserSessionResponse extends BaseAuditResponse {
    
    private Long telegramUserId;
    private UUID userId;
    private String telegramUsername;
    private String telegramFirstName;
    private String telegramLastName;
    private String displayName;
    private String chatId;
    private Boolean isBot;
    private String languageCode;
    private Boolean isPremium;
    
    private LocalDateTime firstInteraction;
    private LocalDateTime lastActivity;
    private Long totalInteractions;
    
    private String currentState;
    private Boolean isRegistered;
    private Boolean isLinkedToUser;
    private Boolean notificationsEnabled;
    private Boolean canReceiveNotifications;
    private String preferredLanguage;
}