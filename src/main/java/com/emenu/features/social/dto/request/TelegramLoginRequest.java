package com.emenu.features.social.dto.request;

import com.emenu.enums.user.UserType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for logging in or registering via Telegram
 * Used for CUSTOMER users who want to quickly register/login via Telegram
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelegramLoginRequest {

    @NotNull(message = "Telegram ID is required")
    private Long telegramId;

    private String telegramUsername;

    private String telegramFirstName;

    private String telegramLastName;

    /**
     * User type for login/registration (defaults to CUSTOMER for Telegram quick registration)
     */
    private UserType userType = UserType.CUSTOMER;

    /**
     * Business ID (only for BUSINESS_USER type)
     */
    private UUID businessId;

    /**
     * Telegram authentication hash to verify the data came from Telegram
     */
    @NotNull(message = "Telegram auth hash is required")
    private String authHash;

    /**
     * Timestamp of the authentication
     */
    @NotNull(message = "Auth timestamp is required")
    private Long authDate;
}
