package com.emenu.enums;

public enum AccountStatus {
    ACTIVE("Active - Account is fully functional"),
    INACTIVE("Inactive - Account temporarily disabled"),
    LOCKED("Locked - Account locked due to security reasons"),
    SUSPENDED("Suspended - Account suspended by platform"),
    PENDING_VERIFICATION("Pending Verification - Email verification required"),
    PENDING_APPROVAL("Pending Approval - Awaiting platform approval"),
    EXPIRED("Expired - Account or subscription has expired"),
    TRIAL("Trial - Account in trial period"),
    CANCELLED("Cancelled - Account cancelled by user"),
    BANNED("Banned - Account permanently banned");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE || this == TRIAL;
    }

    public boolean canLogin() {
        return this == ACTIVE || this == TRIAL || this == PENDING_VERIFICATION;
    }
}