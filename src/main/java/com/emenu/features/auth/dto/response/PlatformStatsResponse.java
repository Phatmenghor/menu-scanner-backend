package com.emenu.features.auth.dto.response;

import lombok.Data;

@Data
public class PlatformStatsResponse {
    
    private Long totalUsers;
    private Long totalBusinesses;
    private Long totalCustomers;
    private Long activeBusinesses;
    private Long suspendedBusinesses;
    private Long totalMessages;
    private Long unreadMessages;
    
    // Subscription stats
    private Long freeSubscriptions;
    private Long basicSubscriptions;
    private Long professionalSubscriptions;
    private Long enterpriseSubscriptions;
    
    // Revenue stats
    private Double monthlyRevenue;
    private Double totalRevenue;
}