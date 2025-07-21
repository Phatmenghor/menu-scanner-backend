package com.emenu.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_PAYMENT("Mobile Payment"),
    CREDIT_CARD("Credit Card");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

}