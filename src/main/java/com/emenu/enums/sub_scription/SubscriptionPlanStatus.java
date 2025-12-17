
package com.emenu.enums.sub_scription;

import lombok.Getter;

@Getter
public enum SubscriptionPlanStatus {
    PUBLIC("Public"),
    PRIVATE("Private");

    private final String description;

    SubscriptionPlanStatus(String description) {
        this.description = description;
    }
}