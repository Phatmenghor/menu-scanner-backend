package com.emenu.enums.auth;

import lombok.Getter;

@Getter
public enum SocialProvider {
    LOCAL("Local", "Traditional userIdentifier/password login", false),
    TELEGRAM("Telegram", "Telegram OAuth2 login with bot integration", true),
    GOOGLE("Google", "Google OAuth2 login", true);

    private final String displayName;
    private final String description;
    private final boolean isSocial;

    SocialProvider(String displayName, String description, boolean isSocial) {
        this.displayName = displayName;
        this.description = description;
        this.isSocial = isSocial;
    }

    public boolean isLocal() {
        return this == LOCAL;
    }

    public boolean isTelegram() {
        return this == TELEGRAM;
    }

    public boolean isGoogle() {
        return this == GOOGLE;
    }

    public boolean requiresPassword() {
        return this == LOCAL;
    }

    public boolean supportsBot() {
        return this == TELEGRAM;
    }
}