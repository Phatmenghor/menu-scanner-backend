package com.emenu.features.usermanagement.dto.response;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.CustomerTier;
import com.emenu.enums.SubscriptionPlan;
import com.emenu.enums.UserType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UserSummaryResponse {
    
    private UUID id;
    private String email;
    private String fullName;
    private String displayName;
    private String phoneNumber;
    private UserType userType;
    private AccountStatus accountStatus;
    private CustomerTier customerTier;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime lastActive;
    private LocalDateTime createdAt;
    
    // Business info
    private UUID businessId;
    private String businessName;
    
    // Subscription info
    private SubscriptionPlan subscriptionPlan;
    private LocalDateTime subscriptionEnds;
    private Boolean hasActiveSubscription;
    
    // Quick stats
    private Integer totalOrders;
    private Double totalSpent;
    private Integer loyaltyPoints;
    
    // Roles
    private List<String> roles;
}