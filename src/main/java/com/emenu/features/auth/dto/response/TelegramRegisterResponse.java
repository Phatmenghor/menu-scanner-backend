package com.emenu.features.auth.dto.response;

import com.emenu.enums.user.UserType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TelegramRegisterResponse {
    
    // User info
    private UUID userId;
    private String userIdentifier;
    private String email;
    private String fullName;
    private String displayName;
    private UserType userType;
    private List<String> roles;
    
    // Telegram info
    private Long telegramUserId;
    private String telegramUsername;
    private String telegramDisplayName;
    private LocalDateTime telegramLinkedAt;
    
    // Registration status
    private Boolean isNewUser = true;
    private String welcomeMessage;
    private List<String> nextSteps;
    private String loginUrl;
}