package com.emenu.enums.notification;

import lombok.Getter;

@Getter
public enum NotificationRecipientType {
    // Individual notifications
    INDIVIDUAL_USER("Individual User", "Sent to specific user"),
    
    // Group notifications
    SYSTEM_OWNER_GROUP("System Owner Group", "All platform owners"),
    BUSINESS_TEAM_GROUP("Business Team Group", "All users in a business"),
    ALL_USERS("All Users", "All registered users in system");

    private final String displayName;
    private final String description;

    NotificationRecipientType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isGroupNotification() {
        return this != INDIVIDUAL_USER;
    }
}