package com.emenu.enums.subdomain;

import lombok.Getter;

@Getter
public enum SubdomainStatus {
    ACTIVE("Active - Domain is live and accessible"),
    SUSPENDED("Suspended - Domain is temporarily disabled"),
    EXPIRED("Expired - Subscription has expired");

    private final String description;

    SubdomainStatus(String description) {
        this.description = description;
    }

    public boolean isAccessible() {
        return this == ACTIVE;
    }
}
