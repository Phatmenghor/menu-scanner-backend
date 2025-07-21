package com.emenu.enums;

import lombok.Getter;

@Getter
public enum BusinessStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    PENDING("Pending Approval");

    private final String description;

    BusinessStatus(String description) {
        this.description = description;
    }

}