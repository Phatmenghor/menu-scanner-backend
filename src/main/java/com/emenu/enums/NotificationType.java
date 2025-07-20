package com.emenu.enums;

public enum NotificationType {
    EMAIL("Email Notification"),
    SMS("SMS Notification"),
    TELEGRAM("Telegram Notification"),
    PUSH("Push Notification"),
    IN_APP("In-App Notification");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}