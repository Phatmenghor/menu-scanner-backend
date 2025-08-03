package com.emenu.enums.order;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("Pending - Waiting for business confirmation"),
    CONFIRMED("Confirmed - Business accepted the order"),
    PREPARING("Preparing - Order is being prepared"),
    READY("Ready - Order is ready for pickup/delivery"),
    OUT_FOR_DELIVERY("Out for Delivery - Order is on the way"),
    DELIVERED("Delivered - Order completed successfully"),
    CANCELLED("Cancelled - Order was cancelled"),
    REJECTED("Rejected - Business rejected the order");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return this == PENDING || this == CONFIRMED || this == PREPARING || this == READY || this == OUT_FOR_DELIVERY;
    }

    public boolean isCompleted() {
        return this == DELIVERED;
    }

    public boolean isCancelled() {
        return this == CANCELLED || this == REJECTED;
    }
}