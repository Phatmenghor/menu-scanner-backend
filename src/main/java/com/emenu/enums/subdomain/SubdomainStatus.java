package com.emenu.enums.subdomain;

import lombok.Getter;

@Getter
public enum SubdomainStatus {
    ACTIVE("Active - Domain is live and accessible"),
    SUSPENDED("Suspended - Domain is temporarily disabled"),
    PENDING("Pending - Domain setup in progress"),
    EXPIRED("Expired - Subscription has expired"),
    BLOCKED("Blocked - Domain blocked due to policy violation");

    private final String description;

    SubdomainStatus(String description) {
        this.description = description;
    }

    public boolean isAccessible() {
        return this == ACTIVE;
    }
}