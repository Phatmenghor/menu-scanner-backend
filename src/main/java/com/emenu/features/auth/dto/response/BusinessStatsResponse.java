package com.emenu.features.auth.dto.response;

import lombok.Data;

@Data
public class BusinessStatsResponse {
    private Integer totalStaff;
    private Integer activeStaff;
    private Integer totalCustomers;
    private Integer totalMessages;
    private Integer unreadMessages;
    private String currentPlan;
    private Boolean subscriptionActive;
    private Integer totalMenuItems;
    private Integer totalTables;
}
