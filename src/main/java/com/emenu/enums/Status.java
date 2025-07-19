package com.emenu.enums;

public enum Status {
    PENDING("Pending verification"),
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    DELETED("Deleted");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}