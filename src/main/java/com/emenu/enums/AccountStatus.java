package com.emenu.enums;

public enum AccountStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    LOCKED("Locked"),
    SUSPENDED("Suspended"),
    PENDING_VERIFICATION("Pending Verification"),
    EXPIRED("Expired");

    private final String displayName;

    AccountStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}