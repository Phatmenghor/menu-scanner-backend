package com.emenu.enums;

public enum NotificationType {
    EMAIL("Email Notification"),
    TELEGRAM("Telegram Notification"),
    BOTH("Both Email and Telegram"),
    NONE("No Notifications");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
