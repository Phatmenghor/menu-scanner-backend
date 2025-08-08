package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TelegramLoginRequest {
    
    @NotNull(message = "Telegram user ID is required")
    private Long telegramUserId;
    
    private String telegramUsername;
    private String telegramFirstName;
    private String telegramLastName;
    private String authDate;
    private String hash;
    private String photoUrl;
    
    private String chatId;
    private String languageCode;
    private Boolean isPremium = false;
}
