package com.emenu.enums.notification;

import lombok.Getter;

@Getter
public enum AlertType {
    SUBSCRIPTION_EXPIRING_SOON("Subscription Expiring Soon", 7),
    SUBSCRIPTION_EXPIRED("Subscription Expired", 0),
    PAYMENT_OVERDUE("Payment Overdue", 3),
    ACCOUNT_SUSPENDED("Account Suspended", 0),
    STAFF_LIMIT_REACHED("Staff Limit Reached", 0),
    MENU_LIMIT_REACHED("Menu Limit Reached", 0),
    WELCOME_NEW_USER("Welcome New User", 0),
    PASSWORD_RESET("Password Reset", 0),
    SECURITY_ALERT("Security Alert", 0);

    private final String description;
    private final int defaultDaysInAdvance;

    AlertType(String description, int defaultDaysInAdvance) {
        this.description = description;
        this.defaultDaysInAdvance = defaultDaysInAdvance;
    }
}