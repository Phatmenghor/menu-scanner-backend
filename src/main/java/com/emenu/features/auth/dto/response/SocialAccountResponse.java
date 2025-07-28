package com.emenu.features.auth.dto.response;

import com.emenu.enums.auth.SocialProvider;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SocialAccountResponse {
    private UUID id;
    private SocialProvider provider;
    private String providerId;
    private String providerUsername;
    private String providerEmail;
    private String providerName;
    private String providerPictureUrl;
    private Boolean isPrimary;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
