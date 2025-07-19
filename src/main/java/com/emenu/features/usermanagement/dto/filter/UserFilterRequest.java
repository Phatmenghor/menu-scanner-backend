package com.emenu.features.usermanagement.dto.filter;

import com.emenu.enums.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class UserFilterRequest {
    
    // Basic filters
    private String search; // Search in name, email, phone, company
    private UserType userType;
    private AccountStatus accountStatus;
    private List<RoleEnum> roles;
    private Boolean hasAnyRole; // If true, user must have ANY of the specified roles
    
    // Customer filters
    private CustomerTier customerTier;
    private Integer minLoyaltyPoints;
    private Integer maxLoyaltyPoints;
    private Double minTotalSpent;
    private Double maxTotalSpent;
    private Integer minTotalOrders;
    private Integer maxTotalOrders;
    
    // Business filters
    private UUID businessId;
    private UUID primaryBusinessId;
    private List<UUID> accessibleBusinessIds;
    private SubscriptionPlan subscriptionPlan;
    private Boolean hasActiveSubscription;
    private Boolean subscriptionExpiringSoon; // Within 30 days
    
    // Platform employee filters
    private String department;
    private String employeeId;
    private LocalDate hiredAfter;
    private LocalDate hiredBefore;
    
    // Authentication filters
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean twoFactorEnabled;
    private LocalDate lastLoginAfter;
    private LocalDate lastLoginBefore;
    private LocalDate lastActiveAfter;
    private LocalDate lastActiveBefore;
    
    // Registration filters
    private LocalDate createdAfter;
    private LocalDate createdBefore;
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private String referralCode;
    
    // Location filters
    private String city;
    private String state;
    private String country;
    
    // Compliance filters
    private Boolean termsAccepted;
    private Boolean privacyAccepted;
    private Boolean dataProcessingConsent;
    private Boolean marketingConsent;
    
    // Notification filters
    private Boolean emailNotifications;
    private Boolean telegramNotifications;
    private Boolean marketingEmails;
    
    // Advanced filters
    private Boolean isActive;
    private Boolean isLocked;
    private Boolean isDeleted = false; // Default to not deleted
    private Integer minSessionCount;
    private Long minTotalLoginTime;
    
    // Pagination
    private Integer pageNo = 0;
    private Integer pageSize = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}