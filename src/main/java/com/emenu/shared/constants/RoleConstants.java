package com.emenu.shared.constants;

/**
 * Constants for role names used throughout the application.
 * Use these constants instead of hardcoded strings to avoid typos and ensure consistency.
 */
public final class RoleConstants {

    private RoleConstants() {
        // Prevent instantiation
    }

    // Platform Roles (Global)
    public static final String PLATFORM_OWNER = "PLATFORM_OWNER";
    public static final String PLATFORM_ADMIN = "PLATFORM_ADMIN";
    public static final String PLATFORM_USER = "PLATFORM_USER";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ADMIN = "ADMIN";

    // Business Roles (Per-Business)
    public static final String BUSINESS_OWNER = "BUSINESS_OWNER";
    public static final String BUSINESS_ADMIN = "BUSINESS_ADMIN";
    public static final String BUSINESS_MANAGER = "BUSINESS_MANAGER";
    public static final String BUSINESS_EMPLOYEE = "BUSINESS_EMPLOYEE";
    public static final String BUSINESS_USER = "BUSINESS_USER";

    // Customer Roles
    public static final String CUSTOMER = "CUSTOMER";
    public static final String CUSTOMER_VIP = "CUSTOMER_VIP";

    // Special Roles
    public static final String GUEST = "GUEST";
    public static final String ANONYMOUS = "ANONYMOUS";

    /**
     * Get all platform role names
     */
    public static String[] getAllPlatformRoles() {
        return new String[]{
            PLATFORM_OWNER,
            PLATFORM_ADMIN,
            PLATFORM_USER,
            SUPER_ADMIN,
            ADMIN
        };
    }

    /**
     * Get all business role names
     */
    public static String[] getAllBusinessRoles() {
        return new String[]{
            BUSINESS_OWNER,
            BUSINESS_ADMIN,
            BUSINESS_MANAGER,
            BUSINESS_EMPLOYEE,
            BUSINESS_USER
        };
    }

    /**
     * Get all customer role names
     */
    public static String[] getAllCustomerRoles() {
        return new String[]{
            CUSTOMER,
            CUSTOMER_VIP
        };
    }
}
