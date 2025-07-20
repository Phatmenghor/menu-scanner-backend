package com.emenu.exception.custom;

public class AccountInactiveException extends AccountStatusException {
    public AccountInactiveException(String message) {
        super(message);
    }
}