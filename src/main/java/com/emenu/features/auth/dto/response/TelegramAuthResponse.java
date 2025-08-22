package com.emenu.features.auth.dto.response;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.enums.user.UserType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TelegramAuthResponse {
    
    // Authentication tokens
    private String accessToken;
    private String tokenType = "Bearer";
    
    // User information
    private UUID userId;
    private String userIdentifier;
    private String email;
    private String fullName;
    private String displayName;
    private UserType userType;
    private List<String> roles;
    
    // Business information (if applicable)
    private UUID businessId;
    private String businessName;
    
    // Telegram-specific information
    private SocialProvider socialProvider;
    private Long telegramUserId;
    private String telegramUsername;
    private String telegramDisplayName;
    private LocalDateTime telegramLinkedAt;
    
    // Status information
    private Boolean isNewUser = false;
    private Boolean hasPasswordSet = false;
    private String welcomeMessage;
    
    // Success message
    private String message;
}