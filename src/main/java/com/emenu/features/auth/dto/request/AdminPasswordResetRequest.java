package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class AdminPasswordResetRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotBlank(message = "New password is required")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String newPassword;
    
    @NotBlank(message = "Password confirmation is required")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String confirmPassword;
}