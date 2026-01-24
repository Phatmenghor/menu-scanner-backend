package com.emenu.enums.social;

import lombok.Getter;

@Getter
public enum SocialAuthProvider {
    TELEGRAM("Telegram", "telegram"),
    GOOGLE("Google", "google"),
    FACEBOOK("Facebook", "facebook"),
    APPLE("Apple", "apple");

    private final String displayName;
    private final String providerKey;

    SocialAuthProvider(String displayName, String providerKey) {
        this.displayName = displayName;
        this.providerKey = providerKey;
    }

    public static SocialAuthProvider fromProviderKey(String providerKey) {
        for (SocialAuthProvider provider : values()) {
            if (provider.providerKey.equalsIgnoreCase(providerKey)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown social auth provider: " + providerKey);
    }
}
