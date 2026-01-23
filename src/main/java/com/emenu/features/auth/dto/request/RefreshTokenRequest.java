package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for refreshing access token using refresh token.
 *
 * @author Cambodia E-Menu Platform
 * @version 1.0.0
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
