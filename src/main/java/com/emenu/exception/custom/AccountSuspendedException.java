package com.emenu.exception.custom;

public class AccountSuspendedException extends AccountStatusException {
    public AccountSuspendedException(String message) {
        super(message);
    }
}