package com.emenu.features.notification.service;

import com.emenu.enums.notification.AlertType;

import java.util.UUID;

public interface AlertService {
    
    // Subscription Alerts
    void checkAndSendSubscriptionAlerts();
    void sendSubscriptionExpiringAlert(UUID businessId, int daysRemaining);
    void sendSubscriptionExpiredAlert(UUID businessId);
    void sendSubscriptionRenewalReminder(UUID businessId);
    
    // Payment Alerts
    void checkAndSendPaymentAlerts();
    void sendPaymentOverdueAlert(UUID businessId);
    void sendPaymentReminderAlert(UUID businessId, int daysOverdue);
    
    // Business Alerts
    void sendBusinessSuspendedAlert(UUID businessId);
    void sendBusinessReactivatedAlert(UUID businessId);
    void sendStaffLimitReachedAlert(UUID businessId);
    void sendMenuLimitReachedAlert(UUID businessId);
    
    // Security Alerts
    void sendSecurityAlert(UUID userId, String alertType, String description);
    void sendLoginAlert(UUID userId, String ipAddress, String location);
    void sendPasswordChangeAlert(UUID userId);
    void sendAccountLockedAlert(UUID userId);
    
    // System Alerts
    void sendSystemMaintenanceAlert();
    void sendSystemUpdateAlert(String updateDetails);
    void sendBackupCompleteAlert();
    
    // Custom Alerts
    void sendCustomAlert(UUID recipientId, AlertType alertType, String title, String message);
    void sendBulkAlert(java.util.List<UUID> recipientIds, AlertType alertType, String title, String message);
    
    // Alert Configuration
    void enableAlert(UUID userId, AlertType alertType);
    void disableAlert(UUID userId, AlertType alertType);
    boolean isAlertEnabled(UUID userId, AlertType alertType);
}