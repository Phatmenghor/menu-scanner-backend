package com.emenu.enums;

public enum RoleEnum {
    // Platform Roles - Those who run the SaaS platform
    PLATFORM_OWNER("Platform Owner - Full platform control and business decisions"),
    PLATFORM_MANAGER("Platform Manager - Platform operations and user management"),
    PLATFORM_STAFF("Platform Staff - Customer support and basic platform tasks"),
    PLATFORM_DEVELOPER("Platform Developer - Technical development and maintenance"),
    PLATFORM_SUPPORT("Platform Support - Customer service and technical support"),
    PLATFORM_SALES("Platform Sales - Sales and marketing activities"),

    // Business Roles - Those who subscribe to use the platform for their business
    BUSINESS_OWNER("Business Owner - Owns restaurant/business, subscribes to platform"),
    BUSINESS_MANAGER("Business Manager - Manages daily business operations"),
    BUSINESS_STAFF("Business Staff - Limited business access for employees"),

    // Customer Roles - End users who interact with businesses
    CUSTOMER("Customer - Regular customer who orders from businesses"),
    VIP_CUSTOMER("VIP Customer - Premium customer with special privileges"),
    GUEST_CUSTOMER("Guest Customer - Browse without full account registration");

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