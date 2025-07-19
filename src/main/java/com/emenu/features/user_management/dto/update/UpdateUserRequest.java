package com.emenu.features.user_management.dto.update;

import com.emenu.enums.RoleEnum;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UUID businessId;
    private List<RoleEnum> roles;
}