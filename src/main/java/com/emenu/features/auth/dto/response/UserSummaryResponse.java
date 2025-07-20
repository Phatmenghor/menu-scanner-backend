package com.emenu.features.auth.dto.response;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.UserType;
import lombok.Data;

import java.util.UUID;

@Data
public class UserSummaryResponse {
    
    private UUID id;
    private String email;
    private String fullName;
    private UserType userType;
    private AccountStatus accountStatus;
    private String businessName;
    private String position;
}