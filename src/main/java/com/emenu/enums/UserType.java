package com.emenu.enums;

import lombok.Getter;

@Getter
public enum UserType {
    PLATFORM_USER("Platform User"),
    BUSINESS_USER("Business User"), 
    CUSTOMER("Customer");

    private final String description;

    UserType(String description) {
        this.description = description;
    }

}