package com.emenu.features.social.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for syncing Telegram account with user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelegramSyncRequest {

    @NotNull(message = "Telegram ID is required")
    private Long telegramId;

    private String telegramUsername;

    private String telegramFirstName;

    private String telegramLastName;

    /**
     * Telegram authentication hash to verify the data came from Telegram
     * This should be validated using your bot token
     */
    @NotNull(message = "Telegram auth hash is required")
    private String authHash;

    /**
     * Timestamp of the authentication (to prevent replay attacks)
     */
    @NotNull(message = "Auth timestamp is required")
    private Long authDate;
}
