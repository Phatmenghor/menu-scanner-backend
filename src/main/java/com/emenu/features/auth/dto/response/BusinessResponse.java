package com.emenu.features.auth.dto.response;

import com.emenu.enums.user.BusinessStatus;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessResponse extends BaseAuditResponse {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String description;
    private String imageUrl;
    private String businessType;

    // Social Media
    private String facebookUrl;
    private String instagramUrl;
    private String telegramUrl;
    
    // Currency & Payment
    private Double usdToKhrRate;
    private Double taxRate;

    // Status & Subscription
    private BusinessStatus status;
    private Boolean isSubscriptionActive;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;
    private Long daysRemaining;
    private Boolean isExpiringSoon;
    
    // Statistics
    private Integer totalStaff;
    private Integer totalCustomers;
    private Integer totalMenuItems;
    private Integer totalTables;
    private Boolean hasActiveSubscription;
    private String currentSubscriptionPlan;
}