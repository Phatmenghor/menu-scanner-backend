package com.emenu.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("Cash Payment", "CASH", true),
    BANK_TRANSFER("Bank Transfer", "BANK", true),
    MOBILE_PAYMENT("Mobile Payment (ABA/Wing/Pi Pay)", "MOBILE", true),
    CREDIT_CARD("Credit Card", "CARD", false), // Future implementation
    PAYPAL("PayPal", "PAYPAL", false), // Future implementation
    STRIPE("Stripe", "STRIPE", false); // Future implementation

    private final String description;
    private final String code;
    private final boolean available; // Available in Cambodia

    PaymentMethod(String description, String code, boolean available) {
        this.description = description;
        this.code = code;
        this.available = available;
    }

    public boolean isAvailableInCambodia() {
        return available;
    }
}