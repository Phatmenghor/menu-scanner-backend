package com.emenu.features.social.dto.response;

import com.emenu.enums.social.SocialAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAuthResponse {

    private boolean success;
    private String message;
    private SocialAuthProvider provider;

    private UUID userId;
    private String userIdentifier;
    private String userType;

    private String accessToken;
    private String refreshToken;

    private String socialId;
    private String socialUsername;
    private LocalDateTime syncedAt;

    private String operationType;
    private boolean isNewUser;
}
