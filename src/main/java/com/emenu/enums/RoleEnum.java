package com.emenu.enums;

public enum RoleEnum {
    // Platform Roles
    PLATFORM_OWNER("Platform Owner"),
    PLATFORM_ADMIN("Platform Administrator"),
    PLATFORM_MANAGER("Platform Manager"),
    PLATFORM_SUPPORT("Platform Support"),
    PLATFORM_DEVELOPER("Platform Developer"),
    PLATFORM_SALES("Platform Sales"),
    
    // Business Roles
    BUSINESS_OWNER("Business Owner"),
    BUSINESS_MANAGER("Business Manager"),
    BUSINESS_STAFF("Business Staff"),
    
    // Customer Roles
    CUSTOMER("Customer"),
    VIP_CUSTOMER("VIP Customer"),
    GUEST_CUSTOMER("Guest Customer");

    private final String description;

    RoleEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // Helper methods to check role hierarchy
    public boolean isPlatformRole() {
        return this.name().startsWith("PLATFORM_");
    }

    public boolean isBusinessRole() {
        return this.name().startsWith("BUSINESS_");
    }

    public boolean isCustomerRole() {
        return this.name().contains("CUSTOMER");
    }

    public boolean hasHigherAuthority(RoleEnum other) {
        return this.getAuthLevel() > other.getAuthLevel();
    }

    private int getAuthLevel() {
        return switch (this) {
            case PLATFORM_OWNER -> 100;
            case PLATFORM_ADMIN -> 90;
            case PLATFORM_MANAGER -> 80;
            case PLATFORM_DEVELOPER -> 70;
            case PLATFORM_SALES -> 60;
            case PLATFORM_SUPPORT -> 50;
            case BUSINESS_OWNER -> 40;
            case BUSINESS_MANAGER -> 30;
            case BUSINESS_STAFF -> 20;
            case VIP_CUSTOMER -> 15;
            case CUSTOMER -> 10;
            case GUEST_CUSTOMER -> 5;
        };
    }
}