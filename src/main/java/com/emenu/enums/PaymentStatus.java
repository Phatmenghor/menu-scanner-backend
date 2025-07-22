package com.emenu.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Pending", "Payment is awaiting processing", false),
    PROCESSING("Processing", "Payment is being processed", false),
    COMPLETED("Completed", "Payment has been successfully processed", true),
    FAILED("Failed", "Payment processing failed", false),
    CANCELLED("Cancelled", "Payment was cancelled", false),
    REFUNDED("Refunded", "Payment has been refunded", false),
    EXPIRED("Expired", "Payment offer has expired", false);

    private final String description;
    private final String message;
    @Getter
    private final boolean successful;

    PaymentStatus(String description, String message, boolean successful) {
        this.description = description;
        this.message = message;
        this.successful = successful;
    }

    public boolean isPending() {
        return this == PENDING || this == PROCESSING;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == REFUNDED || this == EXPIRED;
    }
}