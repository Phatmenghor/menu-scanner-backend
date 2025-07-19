package com.emenu.features.user_management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Email
    private String email;
    
    @NotBlank
    private String password; // Simple validation - just not blank
    
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;
    
    private String phoneNumber;
    private String userType = "BUSINESS_USER"; // Default to business user
}
