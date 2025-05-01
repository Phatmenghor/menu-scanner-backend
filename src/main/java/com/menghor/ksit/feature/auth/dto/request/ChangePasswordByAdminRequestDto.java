package com.menghor.ksit.feature.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordByAdminRequestDto {

    @NotNull(message = "id is required")
    private Long id;

    @Size(min = 6, message = "Password must have at least 6 characters")
    @NotBlank(message = "New password is required")
    private String newPassword;

    @Size(min = 6, message = "Password must have at least 6 characters")
    @NotBlank(message = "Confirm new password is required")
    private String confirmNewPassword;
}