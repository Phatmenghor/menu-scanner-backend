package com.emenu.shared.constants;

public class ErrorCodes {
    // User related errors
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    public static final String USER_DELETED = "USER_DELETED";
    public static final String USER_INACTIVE = "USER_INACTIVE";
    
    // Authentication errors
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
    public static final String EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String TOKEN_INVALID = "TOKEN_INVALID";
    
    // Authorization errors
    public static final String INSUFFICIENT_PERMISSIONS = "INSUFFICIENT_PERMISSIONS";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    
    // Business related errors
    public static final String BUSINESS_NOT_FOUND = "BUSINESS_NOT_FOUND";
    public static final String SUBSCRIPTION_EXPIRED = "SUBSCRIPTION_EXPIRED";
    public static final String SUBSCRIPTION_LIMIT_EXCEEDED = "SUBSCRIPTION_LIMIT_EXCEEDED";
    
    // Validation errors
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    public static final String PHONE_ALREADY_EXISTS = "PHONE_ALREADY_EXISTS";
    
    // System errors
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    public static final String DATABASE_ERROR = "DATABASE_ERROR";
}