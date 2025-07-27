package com.emenu.enums.product;

import lombok.Getter;

@Getter
public enum PromotionType {
    PERCENTAGE("Percentage Discount - %"),
    FIXED_AMOUNT("Fixed Amount Discount - $");

    private final String description;

    PromotionType(String description) {
        this.description = description;
    }

    public boolean isPercentage() {
        return this == PERCENTAGE;
    }

    public boolean isFixedAmount() {
        return this == FIXED_AMOUNT;
    }
}