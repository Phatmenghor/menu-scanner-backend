package com.emenu.features.user_management.dto.request;

import com.emenu.enums.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateBusinessUserRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String password;
    
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;
    
    private String phoneNumber;
    private UUID businessId;
    private RoleEnum role; // BUSINESS_OWNER, BUSINESS_MANAGER, BUSINESS_STAFF
}
