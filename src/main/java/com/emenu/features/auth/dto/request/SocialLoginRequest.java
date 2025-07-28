package com.emenu.features.auth.dto.request;

import com.emenu.enums.auth.SocialProvider;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SocialLoginRequest {
    @NotNull(message = "Provider is required")
    private SocialProvider provider;
    
    // For Google OAuth2 - authorization code
    private String code;
    private String state;
    private String redirectUri;
    
    // For Telegram - login widget data
    private TelegramLoginData telegramData;
}