package com.emenu.features.user_management.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BusinessUserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private UUID businessId;
    private String businessName;
    private Boolean emailVerified;
    private String status;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    
    // Subscription info
    private UUID subscriptionId;
    private String subscriptionPlan;
    private String subscriptionStatus;
    private LocalDateTime subscriptionEndDate;
}