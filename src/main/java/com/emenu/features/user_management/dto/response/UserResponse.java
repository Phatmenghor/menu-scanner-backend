package com.emenu.features.user_management.dto.response;

import com.emenu.enums.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UserResponse {
    
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String displayName;
    private String phoneNumber;
    private GenderEnum gender;
    private LocalDate dateOfBirth;
    private UserType userType;
    private AccountStatus accountStatus;
    
    // Profile
    private String profileImageUrl;
    private String bio;
    private String company;
    private String position;
    
    // Address
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    
    // Preferences
    private String timezone;
    private String language;
    private String currency;
    
    // Authentication status
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean twoFactorEnabled;
    private LocalDateTime lastLogin;
    private LocalDateTime lastActive;
    private Integer loginAttempts;
    private Integer sessionCount;
    
    // Customer specific fields
    private CustomerTier customerTier;
    private Integer loyaltyPoints;
    private Integer totalOrders;
    private Double totalSpent;
    private String favoriteCuisines;
    private String dietaryRestrictions;
    
    // Business relationships
    private UUID businessId;
    private UUID primaryBusinessId;
    private List<UUID> accessibleBusinessIds;
    private String businessName;
    
    // Platform employee info
    private String employeeId;
    private String department;
    private LocalDate hireDate;
    private Double salary;
    private Double commissionRate;
    
    // Subscription info
    private UUID subscriptionId;
    private SubscriptionPlan subscriptionPlan;
    private LocalDateTime subscriptionStarts;
    private LocalDateTime subscriptionEnds;
    private Boolean hasActiveSubscription;
    private Long daysUntilExpiration;
    
    // Notification preferences
    private Boolean emailNotifications;
    private Boolean telegramNotifications;
    private String telegramUserId;
    private String telegramUsername;
    private Boolean marketingEmails;
    private Boolean orderNotifications;
    private Boolean loyaltyNotifications;
    private Boolean platformNotifications;
    private Boolean securityNotifications;
    
    // Roles and permissions
    private List<String> roles;
    private List<String> permissions;
    
    // Marketing
    private String referralCode;
    private UUID referredByUserId;
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    
    // Terms compliance
    private Boolean termsAccepted;
    private LocalDateTime termsAcceptedAt;
    private Boolean privacyAccepted;
    private LocalDateTime privacyAcceptedAt;
    private Boolean dataProcessingConsent;
    private Boolean marketingConsent;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
}