package com.emenu.enums.payment;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("Cash Payment"),
    BANK_TRANSFER("Bank Transfer"),
    ONLINE("Online Payment"),
    OTHER("Other");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }
}