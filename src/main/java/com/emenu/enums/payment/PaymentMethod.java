package com.emenu.enums.payment;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("Cash Payment"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_PAYMENT("Mobile Payment (ABA/Wing/Pi Pay)"),
    ONLINE("Online Payment");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }
}