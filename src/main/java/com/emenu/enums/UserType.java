package com.emenu.enums;

public enum UserType {
    PLATFORM_ADMIN("Platform Administrator - Full system access"),
    BUSINESS_OWNER("Business Owner - Manage their restaurant"),
    BUSINESS_STAFF("Business Staff - Limited business access"),
    CUSTOMER("Customer - Order from businesses"),
    GUEST("Guest User - Browse without account");

    private final String description;

    UserType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}