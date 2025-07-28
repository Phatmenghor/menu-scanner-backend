package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TelegramLoginData {
    @NotNull(message = "Telegram user ID is required")
    private Long id;
    
    private String firstName;
    private String lastName;
    private String username;
    private String photoUrl;
    
    @NotNull(message = "Auth date is required")
    private Long authDate;
    
    @NotNull(message = "Hash is required")
    private String hash;
}
