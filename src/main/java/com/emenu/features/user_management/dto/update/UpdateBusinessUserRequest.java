package com.emenu.features.user_management.dto.update;

import com.emenu.enums.RoleEnum;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateBusinessUserRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UUID businessId;
    private RoleEnum role;
}