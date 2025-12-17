package com.emenu.enums.notification;

import lombok.Getter;

@Getter
public enum MessageType {
    // System Notifications
    SYSTEM_ALERT("System Alert", "Critical system issues"),
    PLATFORM_ANNOUNCEMENT("Platform Announcement", "General platform news"),
    
    // Subscription & Payment
    SUBSCRIPTION_EXPIRY("Subscription Expiry", "Subscription expiring soon"),
    SUBSCRIPTION_RENEWAL("Subscription Renewal", "Subscription renewed"),
    SUBSCRIPTION_EXPIRED("Subscription Expired", "Subscription has expired"),
    PAYMENT_REMINDER("Payment Reminder", "Payment due reminder"),
    PAYMENT_RECEIVED("Payment Received", "Payment confirmation"),
    PAYMENT_FAILED("Payment Failed", "Payment processing failed"),
    
    // Order Management
    ORDER_NEW("New Order", "New order received"),
    ORDER_CONFIRMED("Order Confirmed", "Order confirmed by business"),
    ORDER_PREPARING("Order Preparing", "Order is being prepared"),
    ORDER_READY("Order Ready", "Order ready for pickup/delivery"),
    ORDER_DELIVERED("Order Delivered", "Order delivered successfully"),
    ORDER_CANCELLED("Order Cancelled", "Order cancelled"),
    
    // Business Operations
    BUSINESS_INQUIRY("Business Inquiry", "Business related inquiry"),
    BUSINESS_UPDATE("Business Update", "Business information update"),
    BUSINESS_APPROVED("Business Approved", "Business account approved"),
    BUSINESS_SUSPENDED("Business Suspended", "Business account suspended"),
    
    // User Account
    WELCOME_MESSAGE("Welcome Message", "Welcome new user"),
    ACCOUNT_UPDATE("Account Update", "Account information update"),
    ACCOUNT_VERIFICATION("Account Verification", "Verify your account"),
    PASSWORD_RESET("Password Reset", "Password reset request"),
    ACCOUNT_LOCKED("Account Locked", "Account locked notification"),
    
    // Support & Communication
    SUPPORT_TICKET("Support Ticket", "Support ticket notification"),
    CUSTOMER_SUPPORT("Customer Support", "Customer support message"),
    STAFF_MESSAGE("Staff Message", "Message from staff"),
    
    // Promotions & Marketing
    PROMOTION_ANNOUNCEMENT("Promotion", "Special promotion or offer"),
    DISCOUNT_ALERT("Discount Alert", "Discount available"),
    
    // Reports & Reminders
    DAILY_REPORT("Daily Report", "Daily business report"),
    WEEKLY_REPORT("Weekly Report", "Weekly business report"),
    REMINDER("Reminder", "General reminder");

    private final String displayName;
    private final String description;

    MessageType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    // Helper methods for categorization
    public boolean isSystemNotification() {
        return this == SYSTEM_ALERT || this == PLATFORM_ANNOUNCEMENT;
    }

    public boolean isOrderNotification() {
        return this == ORDER_NEW || this == ORDER_CONFIRMED || 
               this == ORDER_PREPARING || this == ORDER_READY || 
               this == ORDER_DELIVERED || this == ORDER_CANCELLED;
    }

    public boolean isPaymentNotification() {
        return this == PAYMENT_REMINDER || this == PAYMENT_RECEIVED || 
               this == PAYMENT_FAILED;
    }

    public boolean isSubscriptionNotification() {
        return this == SUBSCRIPTION_EXPIRY || this == SUBSCRIPTION_RENEWAL || 
               this == SUBSCRIPTION_EXPIRED;
    }

    public boolean isAccountNotification() {
        return this == WELCOME_MESSAGE || this == ACCOUNT_UPDATE || 
               this == ACCOUNT_VERIFICATION || this == PASSWORD_RESET || 
               this == ACCOUNT_LOCKED;
    }

    public boolean isBusinessNotification() {
        return this == BUSINESS_INQUIRY || this == BUSINESS_UPDATE || 
               this == BUSINESS_APPROVED || this == BUSINESS_SUSPENDED;
    }

    public boolean isSupportNotification() {
        return this == SUPPORT_TICKET || this == CUSTOMER_SUPPORT || 
               this == STAFF_MESSAGE;
    }

    public boolean isPromotionNotification() {
        return this == PROMOTION_ANNOUNCEMENT || this == DISCOUNT_ALERT;
    }

    public boolean isReportNotification() {
        return this == DAILY_REPORT || this == WEEKLY_REPORT;
    }

    // Get icon for frontend (optional)
    public String getIcon() {
        if (isOrderNotification()) return "🛍️";
        if (isPaymentNotification()) return "💳";
        if (isSubscriptionNotification()) return "📅";
        if (isSystemNotification()) return "⚠️";
        if (isAccountNotification()) return "👤";
        if (isBusinessNotification()) return "🏢";
        if (isSupportNotification()) return "💬";
        if (isPromotionNotification()) return "🎁";
        if (isReportNotification()) return "📊";
        return "📢";
    }

    // Get default priority
    public NotificationPriority getDefaultPriority() {
        if (this == SYSTEM_ALERT || this == SUBSCRIPTION_EXPIRED || 
            this == PAYMENT_FAILED || this == ACCOUNT_LOCKED) {
            return NotificationPriority.URGENT;
        }
        if (this == ORDER_NEW || this == PAYMENT_REMINDER || 
            this == SUBSCRIPTION_EXPIRY || this == BUSINESS_SUSPENDED) {
            return NotificationPriority.HIGH;
        }
        if (this == WELCOME_MESSAGE || this == PROMOTION_ANNOUNCEMENT) {
            return NotificationPriority.LOW;
        }
        return NotificationPriority.NORMAL;
    }
}