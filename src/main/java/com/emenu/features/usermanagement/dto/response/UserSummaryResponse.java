package com.emenu.features.usermanagement.dto.response;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.CustomerTier;
import com.emenu.enums.UserType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserSummaryResponse {
    
    private UUID id;
    private String email;
    private String fullName;
    private UserType userType;
    private AccountStatus accountStatus;
    private CustomerTier customerTier;
    private Boolean emailVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}
