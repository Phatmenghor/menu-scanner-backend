package com.emenu.utils.validation;

import com.emenu.exception.ValidationException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class ValidationUtils {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    public static void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ValidationException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    public static void validatePhone(String phone) {
        if (StringUtils.hasText(phone) && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("Invalid phone number format");
        }
    }

    public static void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new ValidationException("Password is required");
        }
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character");
        }
    }

    public static void validateNotBlank(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ValidationException(fieldName + " is required");
        }
    }

    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}
