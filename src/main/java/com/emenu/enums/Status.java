package com.emenu.enums;

public enum Status {
    ACTIVE("Active - Normal operation"),
    INACTIVE("Inactive - Temporarily disabled"),
    PENDING("Pending - Awaiting approval"),
    SUSPENDED("Suspended - Temporarily blocked"),
    DELETED("Deleted - Soft deleted");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
