package com.emenu.enums.notification;

import lombok.Getter;

@Getter
public enum NotificationPriority {
    LOW("Low"),
    NORMAL("Normal"),
    HIGH("High"),
    URGENT("Urgent");

    private final String description;

    NotificationPriority(String description) {
        this.description = description;
    }
}