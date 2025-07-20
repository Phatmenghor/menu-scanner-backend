package com.emenu.features.auth.dto.request;

import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserCreateRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    private String phoneNumber;
    
    @NotNull(message = "User type is required")
    private UserType userType;
    
    private UUID businessId;
    private List<RoleEnum> roles;
    private String position;
    private String address;
    private String notes;
}
