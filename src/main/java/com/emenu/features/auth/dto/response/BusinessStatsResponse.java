package com.emenu.features.auth.dto.response;

import lombok.Data;

@Data
public class BusinessStatsResponse {
    
    private Integer totalStaff;
    private Integer activeStaff;
    private Integer totalCustomers;
    private Integer totalMessages;
    private Integer unreadMessages;
    
    // Current month stats
    private Integer newCustomersThisMonth;
    private Integer messagesThisMonth;
    
    // Subscription info
    private String currentPlan;
    private Boolean subscriptionActive;
    private String subscriptionEndDate;
    private Integer daysRemaining;
}
