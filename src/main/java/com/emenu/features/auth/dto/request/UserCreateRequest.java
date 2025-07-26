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

    // ✅ USER INFORMATION
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
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    // ✅ BUSINESS INFORMATION
    @NotBlank(message = "Business name is required")
    private String businessName;

    @Email(message = "Business email format is invalid")
    private String businessEmail;

    private String businessPhone;
    private String businessAddress;
    private String businessDescription;

    // ✅ SUBDOMAIN INFORMATION (exact input, no formatting)
    @NotBlank(message = "Preferred subdomain is required")
    private String preferredSubdomain;
}
