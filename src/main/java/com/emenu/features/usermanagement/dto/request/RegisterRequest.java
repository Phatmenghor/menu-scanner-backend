package com.emenu.features.usermanagement.dto.request;

import com.emenu.enums.GenderEnum;
import com.emenu.enums.UserType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
    
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
    
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
    
    private String company;
    private String position;
    private String city;
    private String country;
    
    // Business registration (for business users)
    private String businessName;
    private String businessType;
    private String businessAddress;
    
    // Terms and privacy
    @AssertTrue(message = "You must accept the terms and conditions")
    private boolean acceptTerms;
    
    @AssertTrue(message = "You must accept the privacy policy")
    private boolean acceptPrivacy;
    
    // Marketing preferences
    private boolean acceptMarketing = false;
    private boolean dataProcessingConsent = false;
    
    // Referral
    private String referralCode;
    
    // UTM tracking
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
}