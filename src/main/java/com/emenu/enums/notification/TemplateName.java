package com.emenu.enums.notification;

import lombok.Getter;

@Getter
public enum TemplateName {
    SUBSCRIPTION_EXPIRY_WARNING("subscription_expiry_warning"),
    SUBSCRIPTION_EXPIRED("subscription_expired"),
    SUBSCRIPTION_RENEWAL_SUCCESS("subscription_renewal_success"),
    PAYMENT_CONFIRMATION("payment_confirmation"),
    WELCOME_USER("welcome_user"),
    PASSWORD_RESET("password_reset"),
    ACCOUNT_SUSPENDED("account_suspended"),
    SUPPORT_TICKET_CREATED("support_ticket_created"),
    SUPPORT_TICKET_REPLIED("support_ticket_replied");

    private final String templateKey;

    TemplateName(String templateKey) {
        this.templateKey = templateKey;
    }
}