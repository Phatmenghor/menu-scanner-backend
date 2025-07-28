package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "User identifier is required")
    private String userIdentifier; // Can be anything - no validation
    
    @NotBlank(message = "Password is required")
    private String password;
}