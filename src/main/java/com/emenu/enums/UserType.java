package com.emenu.enums;

public enum UserType {
    PLATFORM_USER("Platform User - Works for the SaaS platform company"),
    BUSINESS_USER("Business User - Subscribes to use platform for their business"),
    CUSTOMER("Customer - End user who orders from businesses"),
    GUEST("Guest - Limited access without full registration");

    private final String description;

    UserType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
