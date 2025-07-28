package com.emenu.enums.auth;

import lombok.Getter;

@Getter
public enum SocialProvider {
    LOCAL("Local", "Traditional email/password login"),
    GOOGLE("Google", "Google OAuth2 login"),
    TELEGRAM("Telegram", "Telegram login widget");

    private final String displayName;
    private final String description;

    SocialProvider(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isLocal() {
        return this == LOCAL;
    }

    public boolean isSocial() {
        return this != LOCAL;
    }
}