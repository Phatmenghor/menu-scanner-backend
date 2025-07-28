package com.emenu.features.auth.dto.request;

import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "User identifier is required")
    private String userIdentifier;
    
    private String email; // Optional - can be null

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "User type is required")
    private UserType userType = UserType.CUSTOMER;

    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private String phoneNumber; // Optional - can be null
    private String address;
    private AccountStatus accountStatus = AccountStatus.ACTIVE;
}