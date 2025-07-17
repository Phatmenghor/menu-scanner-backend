package com.emenu.features.usermanagement.dto.request;

import com.emenu.enums.GenderEnum;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    private GenderEnum gender;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "User type is required")
    private UserType userType;

    private String bio;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String timezone = "UTC";
    private String language = "en";

    private UUID businessId;

    @NotEmpty(message = "At least one role is required")
    private List<RoleEnum> roles;

    // Notification preferences
    private Boolean emailNotifications = true;
    private Boolean telegramNotifications = false;
    private String telegramUserId;
    private Boolean marketingEmails = false;
}