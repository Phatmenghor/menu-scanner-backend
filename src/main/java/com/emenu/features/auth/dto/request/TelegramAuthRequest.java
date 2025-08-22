package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TelegramAuthRequest {
    
    @NotNull(message = "Telegram user ID is required")
    private Long telegramUserId;
    
    private String telegramUsername;
    private String telegramFirstName;
    private String telegramLastName;
    private String telegramPhotoUrl;
    private String authDate;
    private String hash;
    
    // Additional Telegram data
    private String chatId;
    private String languageCode;
    private Boolean isPremium = false;
    
    // Optional user data for registration
    private String email;
    private String phoneNumber;
    private String userIdentifier; // Will be generated if not provided
}