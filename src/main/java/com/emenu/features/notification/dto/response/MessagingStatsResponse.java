package com.emenu.features.notification.dto.response;

import lombok.Data;

@Data
public class MessagingStatsResponse {
    
    // Thread Statistics
    private Long totalThreads;
    private Long openThreads;
    private Long closedThreads;
    private Long systemThreads;
    private Long supportTickets;
    
    // Message Statistics
    private Long totalMessages;
    private Long unreadMessages;
    private Long todayMessages;
    private Long weeklyMessages;
    
    // Notification Statistics
    private Long totalNotifications;
    private Long pendingNotifications;
    private Long sentNotifications;
    private Long failedNotifications;
    
    // Channel Statistics
    private Long emailNotifications;
    private Long inAppNotifications;
    private Long telegramNotifications;
    
    // Alert Statistics
    private Long subscriptionAlerts;
    private Long paymentAlerts;
    private Long securityAlerts;
    
    // Response Times
    private Double averageResponseTimeHours;
    private Double averageResolutionTimeHours;
}
