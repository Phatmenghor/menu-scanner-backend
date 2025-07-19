package com.emenu.shared.constants;

public class ValidationMessages {
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_INVALID = "Email format is invalid";
    public static final String EMAIL_EXISTS = "Email already exists";
    
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String PASSWORD_MIN_LENGTH = "Password must be at least 8 characters long";
    public static final String PASSWORD_UPPERCASE = "Password must contain at least one uppercase letter";
    public static final String PASSWORD_LOWERCASE = "Password must contain at least one lowercase letter";
    public static final String PASSWORD_NUMBER = "Password must contain at least one number";
    public static final String PASSWORD_SPECIAL = "Password must contain at least one special character";
    public static final String PASSWORD_CONFIRMATION_MISMATCH = "Password confirmation does not match";
    
    public static final String PHONE_INVALID = "Phone number format is invalid";
    public static final String PHONE_EXISTS = "Phone number already exists";
    
    public static final String NAME_REQUIRED = "Name is required";
    public static final String NAME_TOO_LONG = "Name must not exceed 100 characters";
    
    public static final String DATE_PAST = "Date must be in the past";
    public static final String DATE_FUTURE = "Date must be in the future";
    
    public static final String TERMS_REQUIRED = "You must accept the terms and conditions";
    public static final String PRIVACY_REQUIRED = "You must accept the privacy policy";
}