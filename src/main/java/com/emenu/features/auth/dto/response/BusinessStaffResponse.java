package com.emenu.features.auth.dto.response;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.RoleEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class BusinessStaffResponse {
    
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private AccountStatus accountStatus;
    private String position;
    private String address;
    private String notes;
    private List<RoleEnum> roles;
    private UUID businessId;
    private String businessName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
