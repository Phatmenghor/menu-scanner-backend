package com.emenu.enums.user;

import lombok.Getter;

@Getter
public enum RoleEnum {
    PLATFORM_OWNER("Platform Owner", "Full platform control"),
    BUSINESS_OWNER("Business Owner", "Full business control"),
    CUSTOMER("Customer", "Customer access");

    private final String displayName;
    private final String description;

    RoleEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public RoleScope getScope() {
        return switch (this) {
            case PLATFORM_OWNER -> RoleScope.PLATFORM;
            case BUSINESS_OWNER -> RoleScope.BUSINESS;
            case CUSTOMER -> RoleScope.CUSTOMER;
        };
    }
}