package com.emenu.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    SYSTEM_ALERT("System Alert"),
    SUBSCRIPTION_EXPIRY("Subscription Expiry"),
    SUBSCRIPTION_RENEWAL("Subscription Renewal"),
    PAYMENT_REMINDER("Payment Reminder"),
    SUPPORT_TICKET("Support Ticket"),
    BUSINESS_INQUIRY("Business Inquiry"),
    CUSTOMER_SUPPORT("Customer Support"),
    PLATFORM_ANNOUNCEMENT("Platform Announcement"),
    WELCOME_MESSAGE("Welcome Message"),
    ACCOUNT_UPDATE("Account Update");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }
}