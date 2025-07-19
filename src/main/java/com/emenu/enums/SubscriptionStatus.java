package com.emenu.enums;

public enum SubscriptionStatus {
    ACTIVE("Active"),
    PENDING("Pending"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled"),
    SUSPENDED("Suspended");

    private final String description;

    SubscriptionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
