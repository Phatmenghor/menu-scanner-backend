package com.emenu.features.auth.dto.request;

import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserCreateRequest {

    // ✅ USER INFORMATION ONLY
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profileImageUrl;
    private String position;
    private String address;
    private String notes;
    
    @NotNull(message = "User type is required")
    private UserType userType;
    
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    // ✅ BUSINESS ASSIGNMENT (Optional - for business users)
    private UUID businessId; // Assign user to existing business
    
    // ✅ ROLES (Required)
    @NotNull(message = "At least one role is required")
    private List<RoleEnum> roles;
}