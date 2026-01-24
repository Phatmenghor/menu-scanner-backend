package com.emenu.features.social.dto.request;

import com.emenu.enums.user.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for Google OAuth authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthRequest {

    /**
     * Google ID token received from Google OAuth
     */
    @NotBlank(message = "Google ID token is required")
    private String idToken;

    /**
     * User type for login/registration
     */
    @NotNull(message = "User type is required")
    private UserType userType;

    /**
     * Business ID (required only for BUSINESS_USER type)
     */
    private UUID businessId;

    /**
     * Flag to indicate if this is a sync request (true) or login/register request (false)
     * - true: sync Google account to existing logged-in user
     * - false: login or register using Google account
     */
    private Boolean isSyncOnly = false;
}
