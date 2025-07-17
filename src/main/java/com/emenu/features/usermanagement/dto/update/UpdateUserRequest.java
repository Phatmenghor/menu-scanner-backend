package com.emenu.features.usermanagement.dto.update;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.GenderEnum;
import com.emenu.enums.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateUserRequest {

    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    private GenderEnum gender;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private AccountStatus accountStatus;

    private String bio;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String timezone;
    private String language;

    private UUID businessId;
    private List<RoleEnum> roles;

    // Notification preferences
    private Boolean emailNotifications;
    private Boolean telegramNotifications;
    private String telegramUserId;
    private String telegramChatId;
    private Boolean marketingEmails;
    private Boolean orderNotifications;
    private Boolean loyaltyNotifications;
}
