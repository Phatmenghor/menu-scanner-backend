package com.emenu.enums;

public enum BusinessStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    PENDING("Pending Approval");

    private final String description;

    BusinessStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
