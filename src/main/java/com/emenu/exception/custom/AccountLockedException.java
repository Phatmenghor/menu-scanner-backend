package com.emenu.exception.custom;

public class AccountLockedException extends AccountStatusException {
    public AccountLockedException(String message) {
        super(message);
    }
}