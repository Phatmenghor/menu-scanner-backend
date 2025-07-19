package com.emenu.utils.validation;

import com.emenu.exception.ValidationException;
import org.springframework.util.StringUtils;

public class ValidationUtils {
    
    public static void validateEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            throw new ValidationException("Invalid email format");
        }
    }

    public static void validatePassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }
    }

    public static void validateNotBlank(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ValidationException(fieldName + " is required");
        }
    }
}