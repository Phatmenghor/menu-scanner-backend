package com.emenu.features.auth.dto.request;

import com.emenu.enums.auth.SocialProvider;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LinkSocialAccountRequest {
    @NotNull(message = "Provider is required")
    private SocialProvider provider;
    
    // For Google OAuth2
    private String code;
    private String redirectUri;
    
    // For Telegram
    private TelegramLoginData telegramData;
    
    // Make this account primary for login
    private Boolean makePrimary = false;
}