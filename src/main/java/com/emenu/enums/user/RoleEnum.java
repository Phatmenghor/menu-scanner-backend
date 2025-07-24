package com.emenu.enums.user;

import lombok.Getter;

@Getter
public enum RoleEnum {
    // Platform Roles
    PLATFORM_OWNER("Platform Owner", "Full platform control", true, false, false),
    PLATFORM_ADMIN("Platform Admin", "Platform administration", true, false, false),
    PLATFORM_MANAGER("Platform Manager", "Platform operations", true, false, false),
    PLATFORM_SUPPORT("Platform Support", "Customer support", true, false, false),
    
    // Business Roles
    BUSINESS_OWNER("Business Owner", "Business management", false, true, false),
    BUSINESS_MANAGER("Business Manager", "Business operations", false, true, false),
    BUSINESS_STAFF("Business Staff", "Basic business access", false, true, false),
    
    // Customer Roles
    CUSTOMER("Customer", "Customer access", false, false, true);

    private final String displayName;
    private final String description;
    private final boolean platformRole;
    private final boolean businessRole;
    private final boolean customerRole;

    RoleEnum(String displayName, String description, boolean platformRole, boolean businessRole, boolean customerRole) {
        this.displayName = displayName;
        this.description = description;
        this.platformRole = platformRole;
        this.businessRole = businessRole;
        this.customerRole = customerRole;
    }

    public boolean isPlatformRole() {
        return platformRole;
    }

    public boolean isBusinessRole() {
        return businessRole;
    }

    public boolean isCustomerRole() {
        return customerRole;
    }
}