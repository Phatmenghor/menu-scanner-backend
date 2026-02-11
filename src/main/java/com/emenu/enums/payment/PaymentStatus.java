package com.emenu.enums.payment;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    // Payment transaction statuses
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),

    // Order payment statuses
    PAID("Paid"),
    UNPAID("Unpaid"),
    REFUNDED("Refunded");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isPaid() {
        return this == PAID || this == COMPLETED;
    }

    public boolean isUnpaid() {
        return this == UNPAID || this == PENDING;
    }

    public boolean isRefunded() {
        return this == REFUNDED;
    }
}