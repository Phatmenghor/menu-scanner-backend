package com.emenu.enums.notification;

import lombok.Getter;

@Getter
public enum NotificationChannel {
    IN_APP("In-App"),
    EMAIL("Email"),
    TELEGRAM("Telegram"),
    SMS("SMS"),
    PUSH("Push Notification");

    private final String description;

    NotificationChannel(String description) {
        this.description = description;
    }
}