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
}
