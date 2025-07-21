package com.emenu.features.notification.service;

public interface ScheduledMessagingService {
    
    // Automated Alerts
    void processSubscriptionAlerts();
    void processPaymentReminders();
    void processSystemAlerts();
    
    // Batch Processing
    void processPendingNotifications();
    void retryFailedNotifications();
    void cleanupOldNotifications();
    
    // Health Checks
    void performHealthChecks();
    void generateDailyReports();
    void generateWeeklyReports();
    
    // Maintenance
    void performDatabaseCleanup();
    void archiveOldData();
    void optimizeNotificationQueues();
}