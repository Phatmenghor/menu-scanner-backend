package com.emenu.enums.user;

import lombok.Getter;

@Getter
public enum AccountStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    LOCKED("Locked"),
    SUSPENDED("Suspended");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }
}