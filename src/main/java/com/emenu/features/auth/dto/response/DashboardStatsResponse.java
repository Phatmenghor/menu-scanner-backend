package com.emenu.features.auth.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardStatsResponse {
    
    // Platform Stats
    private Long totalUsers;
    private Long totalBusinesses;
    private Long totalCustomers;
    private Long activeSubscriptions;
    private Long expiredSubscriptions;
    private Long expiringSoonSubscriptions;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    
    // Business Stats (when viewing business dashboard)
    private Integer totalStaff;
    private Integer activeStaff;
    private Integer totalMenuItems;
    private Integer totalTables;
    private Integer totalOrders;
    private BigDecimal totalSales;
    
    // Subscription Stats
    private String currentPlan;
    private Long daysRemaining;
    private Boolean canAddStaff;
    private Boolean canAddMenuItem;
    private Boolean canAddTable;
    private Integer staffUsage;
    private Integer menuItemsUsage;
    private Integer tablesUsage;
    private Integer staffLimit;
    private Integer menuItemsLimit;
    private Integer tablesLimit;
}