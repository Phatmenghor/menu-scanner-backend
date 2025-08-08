package com.emenu.features.auth.dto.response;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.enums.user.UserType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TelegramLoginResponse {
    
    // Authentication
    private String accessToken;
    private String tokenType = "Bearer";
    
    // User info
    private UUID userId;
    private String userIdentifier;
    private String email;
    private String fullName;
    private String displayName;
    private String profileImageUrl;
    private UserType userType;
    private List<String> roles;
    
    // Business info
    private UUID businessId;
    private String businessName;
    
    // Telegram info
    private SocialProvider socialProvider;
    private Long telegramUserId;
    private String telegramUsername;
    private String telegramDisplayName;
    private LocalDateTime telegramLinkedAt;
    private Boolean telegramNotificationsEnabled;
    
    // Status
    private Boolean isNewUser = false;
    private Boolean hasPasswordSet = false;
    private String welcomeMessage;
    private List<String> availableFeatures;
}