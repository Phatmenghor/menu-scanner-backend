package com.emenu.features.user_management.dto.response;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.CustomerTier;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import lombok.Data;

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
    private UserType userType;
    private AccountStatus accountStatus;
    private UUID businessId;
    private String businessName;
    private List<RoleEnum> roles;
    private CustomerTier customerTier;
    private Integer loyaltyPoints;
    private String position;
    private Double salary;
    private String address;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}