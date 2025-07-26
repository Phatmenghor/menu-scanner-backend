package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeRequest {
    
    @NotBlank(message = "Current password is required")
    private String currentPassword;
    
    @NotBlank(message = "New password is required")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String newPassword;
    
    @NotBlank(message = "Password confirmation is required")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String confirmPassword;
}
