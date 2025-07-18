package com.emenu.features.user_management.dto.filter;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.CustomerTier;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import lombok.Data;

import java.util.UUID;

@Data
public class UserFilterRequest {
    
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserType userType;
    private AccountStatus accountStatus;
    private UUID businessId;
    private RoleEnum role;
    private CustomerTier customerTier;
    private String position;
    private String search; // Global search
    
    // Pagination
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
