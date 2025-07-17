package com.emenu.features.usermanagement.dto.response;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.CustomerTier;
import com.emenu.enums.GenderEnum;
import com.emenu.enums.UserType;
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
    private String phoneNumber;
    private GenderEnum gender;
    private LocalDate dateOfBirth;
    private UserType userType;
    private AccountStatus accountStatus;
    private String profileImageUrl;
    private String bio;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String timezone;
    private String language;
    
    // Authentication status
    private Boolean emailVerified;
    private Boolean twoFactorEnabled;
    private LocalDateTime lastLogin;
    private Integer loginAttempts;
    
    // Customer specific fields
    private CustomerTier customerTier;
    private Integer loyaltyPoints;
    private Integer totalOrders;
    private Double totalSpent;
    
    // Business relationship
    private UUID businessId;
    private String businessName;
    
    // Notification preferences
    private Boolean emailNotifications;
    private Boolean telegramNotifications;
    private Boolean marketingEmails;
    private Boolean orderNotifications;
    private Boolean loyaltyNotifications;
    
    // Roles
    private List<String> roles;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}