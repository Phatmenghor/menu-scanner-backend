package com.emenu.features.auth.dto.response;

import com.emenu.enums.AccountStatus;
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
    private String position;
    private String address;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}