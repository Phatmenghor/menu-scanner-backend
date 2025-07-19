package com.emenu.features.services.dto.response;

import com.emenu.enums.SubscriptionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userName;
    private UUID planId;
    private String planName;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime nextBillingDate;
    private BigDecimal amount;
    private String currency;
    private Boolean autoRenew;
    
    // Usage info
    private Integer currentUsers;
    private Integer currentMenus;
    private Integer currentMonthOrders;
    
    // Plan limits
    private Integer maxUsers;
    private Integer maxMenus;
    private Integer maxOrdersPerMonth;
    
    private LocalDateTime createdAt;
}
