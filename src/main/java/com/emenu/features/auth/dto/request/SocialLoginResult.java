package com.emenu.features.auth.dto.request;

import com.emenu.enums.auth.SocialProvider;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SocialLoginResult {
    private boolean success;
    private String errorMessage;
    private SocialProvider provider;
    private String providerId;
    private String email;
    private String name;
    private String username;
    private String pictureUrl;
    private String providerData;
    
    public static SocialLoginResult success(SocialProvider provider, String providerId, 
                                          String email, String name, String username, 
                                          String pictureUrl, String providerData) {
        return SocialLoginResult.builder()
                .success(true)
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .name(name)
                .username(username)
                .pictureUrl(pictureUrl)
                .providerData(providerData)
                .build();
    }
    
    public static SocialLoginResult failure(String errorMessage) {
        return SocialLoginResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}