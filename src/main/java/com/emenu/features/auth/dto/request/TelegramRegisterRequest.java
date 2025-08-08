package com.emenu.features.auth.dto.request;

import com.emenu.enums.user.UserType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TelegramRegisterRequest {
    
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
    
    // Platform user data
    private String userIdentifier; // Optional - will generate if not provided
    private String email; // Optional
    private String phoneNumber; // Optional
    private String firstName; // Will use telegramFirstName if not provided
    private String lastName; // Will use telegramLastName if not provided
    
    @NotNull(message = "User type is required")
    private UserType userType = UserType.CUSTOMER;
}