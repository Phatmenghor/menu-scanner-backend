package com.emenu.features.auth.dto.update;

import com.emenu.enums.AccountStatus;
import lombok.Data;

import java.util.List;

@Data
public class PlatformUserUpdateRequest {
    
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String position;
    private String address;
    private String notes;
    private AccountStatus accountStatus;
    private List<RoleEnum> roles;
}
