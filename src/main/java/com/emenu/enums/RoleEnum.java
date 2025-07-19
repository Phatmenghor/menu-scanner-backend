package com.emenu.enums;

public enum RoleEnum {
    PLATFORM_OWNER("Platform Owner"),
    PLATFORM_ADMIN("Platform Admin"),
    PLATFORM_SUPPORT("Platform Support"),
    BUSINESS_OWNER("Business Owner"),
    BUSINESS_MANAGER("Business Manager"),
    BUSINESS_STAFF("Business Staff"),
    CUSTOMER("Customer");

    private final String description;

    RoleEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}