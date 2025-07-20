package com.emenu.features.auth.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BusinessSettingsResponse {
    
    private UUID businessId;
    private String name;
    
    // Business Display Settings
    private String logoUrl;
    private String description;
    private String phone;
    private String address;
    private String website;
    private String businessType;
    private String cuisineType;
    private String operatingHours;
    
    // Social Media
    private String facebookUrl;
    private String instagramUrl;
    private String telegramContact;
    
    // Currency Exchange Rate (Frontend will use this for calculations)
    private Double usdToKhrRate;
    
    // Pricing Settings
    private Double taxRate;
    private Double serviceChargeRate;
    
    // Payment Methods
    private Boolean acceptsOnlinePayment;
    private Boolean acceptsCashPayment;
    private Boolean acceptsBankTransfer;
    private Boolean acceptsMobilePayment;
    
    // Subscription Info (Read-only for business)
    private Boolean hasActiveSubscription;
    private String currentPlan;
    private Long daysRemaining;
    private LocalDateTime subscriptionEndDate;
    
    // System Info
    private String currency; // Always "USD" for Cambodia
    private String timezone; // Always "Asia/Phnom_Penh"
    
    // Last Updated
    private LocalDateTime updatedAt;
}