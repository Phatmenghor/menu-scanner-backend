package com.emenu.enums;

public enum RoleEnum {
    // Platform Roles
    PLATFORM_OWNER("Platform Owner"),
    PLATFORM_ADMIN("Platform Admin"),
    PLATFORM_SUPPORT("Platform Support"),

    // Business Roles
    BUSINESS_OWNER("Business Owner"),
    BUSINESS_MANAGER("Business Manager"),
    BUSINESS_STAFF("Business Staff"),

    // Customer Roles
    CUSTOMER("Customer"),
    VIP_CUSTOMER("VIP Customer");

    private final String description;

    RoleEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPlatformRole() {
        return name().startsWith("PLATFORM_");
    }

    public boolean isBusinessRole() {
        return name().startsWith("BUSINESS_");
    }

    public boolean isCustomerRole() {
        return name().contains("CUSTOMER");
    }
}