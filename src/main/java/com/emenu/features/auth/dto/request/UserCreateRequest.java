package com.emenu.features.auth.dto.request;

import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserCreateRequest {

    // ✅ NEW: Required userIdentifier for login (no validation - can be anything)
    @NotBlank(message = "User identifier is required")
    private String userIdentifier;

    private String email; // Optional - can be null

    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 100, message = "Password must be between 4 and 100 characters")
    private String password;

    private String firstName;
    private String lastName;
    private String phoneNumber; // Optional - can be null
    private String profileImageUrl;
    private String position;
    private String address;
    private String notes;
    
    @NotNull(message = "User type is required")
    private UserType userType;
    
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    private UUID businessId; // Assign user to existing business
    
    @NotNull(message = "At least one role is required")
    private List<RoleEnum> roles;
}