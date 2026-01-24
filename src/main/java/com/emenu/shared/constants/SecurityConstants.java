package com.emenu.shared.constants;

/**
 * Constants for security-related strings
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Prevent instantiation
    }

    // Token Types
    public static final String TOKEN_TYPE_BEARER = "Bearer";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    // Session/Device Info
    public static final String DEVICE_TYPE_WEB = "WEB";
    public static final String DEVICE_TYPE_MOBILE = "MOBILE";
    public static final String DEVICE_TYPE_TABLET = "TABLET";
    public static final String DEVICE_TYPE_DESKTOP = "DESKTOP";
    public static final String DEVICE_TYPE_UNKNOWN = "UNKNOWN";

    // Headers
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String HEADER_X_REAL_IP = "X-Real-IP";
    public static final String HEADER_DEVICE_ID = "X-Device-ID";
    public static final String HEADER_DEVICE_NAME = "X-Device-Name";

    // Role Prefix
    public static final String ROLE_PREFIX = "ROLE_";

    // Session Status
    public static final String SESSION_STATUS_ACTIVE = "ACTIVE";
    public static final String SESSION_STATUS_EXPIRED = "EXPIRED";
    public static final String SESSION_STATUS_REVOKED = "REVOKED";
    public static final String SESSION_STATUS_LOGGED_OUT = "LOGGED_OUT";
}
