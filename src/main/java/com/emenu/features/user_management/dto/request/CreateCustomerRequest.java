package com.emenu.features.user_management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCustomerRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String password;
    
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;
    
    private String phoneNumber;
}