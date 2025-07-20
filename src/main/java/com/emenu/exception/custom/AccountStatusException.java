package com.emenu.exception.custom;

import org.springframework.security.core.AuthenticationException;

public class AccountStatusException extends AuthenticationException {
    public AccountStatusException(String message) {
        super(message);
    }
    
    public AccountStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}