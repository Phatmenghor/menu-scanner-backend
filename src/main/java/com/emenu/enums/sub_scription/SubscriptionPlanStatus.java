package com.emenu.enums.sub_scription;

import lombok.Getter;

@Getter
public enum SubscriptionPlanStatus {
    PUBLIC("Public - Available for all businesses"),
    PRIVATE("Private - Custom plan for specific business");

    private final String description;

    SubscriptionPlanStatus(String description) {
        this.description = description;
    }
}
