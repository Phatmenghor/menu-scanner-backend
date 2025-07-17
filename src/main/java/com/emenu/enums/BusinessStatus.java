package com.emenu.enums;

public enum BusinessStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    PENDING_APPROVAL("Pending Approval"),
    TRIAL("Trial Period"),
    EXPIRED("Subscription Expired");

    private final String displayName;

    BusinessStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
