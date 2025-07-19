package com.emenu.enums;

public enum BusinessStatus {
    ACTIVE("Active - Business is fully operational"),
    INACTIVE("Inactive - Business temporarily closed"),
    SUSPENDED("Suspended - Business suspended by platform"),
    PENDING_APPROVAL("Pending Approval - Awaiting platform approval"),
    TRIAL("Trial Period - Using trial subscription"),
    EXPIRED("Subscription Expired - Payment required"),
    CANCELLED("Cancelled - Business account cancelled"),
    BANNED("Banned - Business permanently banned");

    private final String description;

    BusinessStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOperational() {
        return this == ACTIVE || this == TRIAL;
    }
}