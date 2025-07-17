package com.emenu.feature.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Size(min = 3, message = "New password must have at least 3 characters")
    @NotBlank(message = "New password is required")
    private String newPassword;

    @Size(min = 3, message = "Confirm password must have at least 3 characters")
    @NotBlank(message = "Confirm new password is required")
    private String confirmNewPassword;
}