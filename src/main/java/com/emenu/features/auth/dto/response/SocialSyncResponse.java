package com.emenu.features.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for social account sync operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialSyncResponse {

    private boolean success;

    private String message;

    private String provider; // "telegram" or "google"

    private LocalDateTime syncedAt;

    // Telegram specific fields
    private Long telegramId;
    private String telegramUsername;

    // Google specific fields
    private String googleId;
    private String googleEmail;

    public static SocialSyncResponse telegramSuccess(Long telegramId, String telegramUsername, LocalDateTime syncedAt) {
        return SocialSyncResponse.builder()
                .success(true)
                .message("Telegram account synced successfully")
                .provider("telegram")
                .telegramId(telegramId)
                .telegramUsername(telegramUsername)
                .syncedAt(syncedAt)
                .build();
    }

    public static SocialSyncResponse googleSuccess(String googleId, String googleEmail, LocalDateTime syncedAt) {
        return SocialSyncResponse.builder()
                .success(true)
                .message("Google account synced successfully")
                .provider("google")
                .googleId(googleId)
                .googleEmail(googleEmail)
                .syncedAt(syncedAt)
                .build();
    }
}
