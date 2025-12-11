package com.emenu.enums.sub_scription;

import lombok.Getter;

@Getter
public enum SubscriptionStatus {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    EXPIRING_SOON("Expiring Soon");

    private final String description;

    SubscriptionStatus(String description) {
        this.description = description;
    }
}