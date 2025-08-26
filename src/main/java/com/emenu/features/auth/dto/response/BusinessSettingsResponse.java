package com.emenu.features.auth.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BusinessSettingsResponse {
    
    private UUID businessId;
    private String name;
    
    // Business Display Settings
    private String imageUrl;
    private String description;
    private String phone;
    private String address;
    private String businessType;

    // Social Media
    private String facebookUrl;
    private String instagramUrl;
    private String telegramUrl;
    
    private Double usdToKhrRate;
    
    // Pricing Settings
    private Double taxRate;
    
    // Subscription Info (Read-only for business)
    private Boolean hasActiveSubscription;
    private String currentPlan;
    private Long daysRemaining;
    private LocalDateTime subscriptionEndDate;
    
    // System Info
    private String currency; // Always "USD" for Cambodia
    private String timezone; // Always "Asia/Phnom_Penh"

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}