package com.emenu.enums;

public enum RoleEnum {
    SUPER_ADMIN("Super Administrator - Full platform control"),
    PLATFORM_ADMIN("Platform Administrator - Platform management"),
    BUSINESS_OWNER("Business Owner - Own business management"),
    BUSINESS_MANAGER("Business Manager - Business operations"),
    BUSINESS_STAFF("Business Staff - Limited business access"),
    CUSTOMER("Customer - Order and review"),
    GUEST("Guest - Browse without account");

    private final String description;

    RoleEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}