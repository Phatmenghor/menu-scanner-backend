package com.emenu.enums.subdomain;

import lombok.Getter;

@Getter
public enum VerificationStatus {
    PENDING("Pending Verification"),
    VERIFIED("Verified"),
    FAILED("Verification Failed");

    private final String description;

    VerificationStatus(String description) {
        this.description = description;
    }
}