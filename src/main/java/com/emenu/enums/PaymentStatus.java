package com.emenu.enums;

public enum PaymentStatus {
    PENDING("Pending Payment"),
    PROCESSING("Processing Payment"),
    COMPLETED("Payment Completed"),
    FAILED("Payment Failed"),
    CANCELLED("Payment Cancelled"),
    REFUNDED("Payment Refunded"),
    PARTIALLY_REFUNDED("Partially Refunded"),
    EXPIRED("Payment Expired"),
    DISPUTED("Payment Disputed");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == REFUNDED;
    }
}