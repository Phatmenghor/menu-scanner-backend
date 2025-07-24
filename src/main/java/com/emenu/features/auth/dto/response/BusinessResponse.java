package com.emenu.features.auth.dto.response;

import com.emenu.enums.user.BusinessStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BusinessResponse {
    
    // Basic Information
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String description;
    private String logoUrl;
    private String website;
    private String businessType;
    private String cuisineType;
    private String operatingHours;
    
    // Social Media
    private String facebookUrl;
    private String instagramUrl;
    private String telegramContact;
    
    // Currency & Payment
    private Double usdToKhrRate;
    private Double taxRate;
    private Double serviceChargeRate;
    private Boolean acceptsOnlinePayment;
    private Boolean acceptsCashPayment;
    private Boolean acceptsBankTransfer;
    private Boolean acceptsMobilePayment;
    
    // Status & Subscription
    private BusinessStatus status;
    private Boolean isSubscriptionActive;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;
    private Long daysRemaining;
    private Boolean isExpiringSoon;
    
    // Audit Information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Statistics
    private Integer totalStaff;
    private Integer totalCustomers;
    private Integer totalMenuItems;
    private Integer totalTables;
    private Boolean hasActiveSubscription;
    private String currentSubscriptionPlan;
}