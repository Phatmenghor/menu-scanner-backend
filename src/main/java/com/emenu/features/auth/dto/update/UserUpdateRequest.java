package com.emenu.features.auth.dto.update;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.RoleEnum;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserUpdateRequest {
    
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private AccountStatus accountStatus;
    private UUID businessId;
    private List<RoleEnum> roles;
    private String position;
    private String address;
    private String notes;
}
