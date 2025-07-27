package com.emenu.enums.product;

import lombok.Getter;

@Getter
public enum ProductStatus {
    ACTIVE("Active - Available for customers"),
    INACTIVE("Inactive - Hidden from customers"),
    OUT_OF_STOCK("Out of Stock - Temporarily unavailable");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isAvailable() {
        return this == ACTIVE || this == OUT_OF_STOCK;
    }
}