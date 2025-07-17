package com.emenu.features.usermanagement.dto.filter;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.CustomerTier;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class UserFilterRequest {
    
    private String search; // Search in name, email, phone
    private UserType userType;
    private AccountStatus accountStatus;
    private List<RoleEnum> roles;
    private CustomerTier customerTier;
    private UUID businessId;
    private Boolean emailVerified;
    private Boolean twoFactorEnabled;
    private LocalDate createdAfter;
    private LocalDate createdBefore;
    private LocalDate lastLoginAfter;
    private LocalDate lastLoginBefore;
    private Integer minLoyaltyPoints;
    private Integer maxLoyaltyPoints;
    private Double minTotalSpent;
    private Double maxTotalSpent;
    
    // Pagination
    private Integer pageNo = 0;
    private Integer pageSize = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}