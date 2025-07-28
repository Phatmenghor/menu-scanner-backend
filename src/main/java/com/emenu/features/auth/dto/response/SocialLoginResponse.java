package com.emenu.features.auth.dto.response;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.enums.user.UserType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SocialLoginResponse {
    private String accessToken;
    private String tokenType;
    private UUID userId;
    private String userIdentifier;
    private String email;
    private String fullName;
    private String profileImageUrl;
    private UserType userType;
    private List<String> roles;
    private UUID businessId;
    private String businessName;
    private String welcomeMessage;
    
    // Social login specific
    private SocialProvider provider;
    private boolean isNewUser;
    private boolean accountLinked;
    private String socialUsername;
}