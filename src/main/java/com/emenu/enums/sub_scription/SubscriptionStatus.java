package com.emenu.enums.sub_scription;

import lombok.Getter;

@Getter
public enum SubscriptionStatus {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled"),
    PENDING("Pending"),
    SUSPENDED("Suspended"),
    TRIAL("Trial");

    private final String description;

    SubscriptionStatus(String description) {
        this.description = description;
    }
}