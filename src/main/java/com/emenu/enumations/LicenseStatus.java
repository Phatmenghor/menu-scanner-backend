package com.emenu.enumations;

public enum LicenseStatus {
    PENDING, ACTIVE, EXPIRED, SUSPENDED, CANCELLED, TRIAL, GRACE_PERIOD;

    public boolean isAccessible() {
        return this == ACTIVE || this == TRIAL || this == GRACE_PERIOD;
    }
}