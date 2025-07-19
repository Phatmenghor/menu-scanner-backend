package com.emenu.features.user_management.dto.request;

import com.emenu.enums.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;

    // Profile
    private String bio;
    private String company;
    private String position;
    
    // Address
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    
    // Preferences
    private String timezone = "UTC";
    private String language = "en";
    private String currency = "USD";

    // Business relationships
    private UUID businessId;
    private UUID primaryBusinessId;
    private List<UUID> accessibleBusinessIds;

    // Platform employee info (for platform users)
    private String employeeId;
    private String department;
    private LocalDate hireDate;
    private Double salary;
    private Double commissionRate;

    // Subscription (for business owners)
    private SubscriptionPlan subscriptionPlan;
    private LocalDateTime subscriptionStarts;
    private LocalDateTime subscriptionEnds;

    @NotEmpty(message = "At least one role is required")
    private List<RoleEnum> roles;

    // Notification preferences
    private Boolean emailNotifications = true;
    private Boolean telegramNotifications = false;
    private String telegramUserId;
    private Boolean marketingEmails = false;
    private Boolean platformNotifications = true;

    // Verification
    private Boolean emailVerified = false;
    private Boolean phoneVerified = false;

    // Terms
    private Boolean termsAccepted = false;
    private Boolean privacyAccepted = false;
    private Boolean dataProcessingConsent = false;
    private Boolean marketingConsent = false;
}