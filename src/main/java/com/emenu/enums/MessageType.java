package com.emenu.enums;

public enum MessageType {
    GENERAL("General Message"),
    WELCOME("Welcome Message"),
    NOTIFICATION("System Notification"),
    ANNOUNCEMENT("Platform Announcement"),
    BUSINESS_UPDATE("Business Update"),
    SUBSCRIPTION_ALERT("Subscription Alert"),
    SUPPORT("Support Message");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
