package com.emenu.enums.sub_scription;

import lombok.Getter;

@Getter
public enum SubscriptionPlanStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    CUSTOM("Custom"),
    TRIAL("Trial"),
    DEFAULT("Default"),
    ARCHIVED("Archived");

    private final String description;

    SubscriptionPlanStatus(String description) {
        this.description = description;
    }
}