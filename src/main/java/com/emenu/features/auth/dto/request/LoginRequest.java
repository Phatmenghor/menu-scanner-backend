package com.emenu.features.auth.dto.request;

import com.emenu.enums.user.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Login request with context for dynamic username uniqueness.
 *
 * Username uniqueness rules:
 * - The SAME username can exist as CUSTOMER, PLATFORM_USER, and in multiple businesses
 * - Example: "john" can be:
 *   ✅ ONE customer (globally unique among customers)
 *   ✅ ONE platform user (globally unique among platform users)
 *   ✅ Business user in Business A
 *   ✅ Business user in Business B
 *   ✅ Business user in Business C, etc.
 *
 * Login requirements:
 * - CUSTOMER: userIdentifier + userType=CUSTOMER
 * - PLATFORM_USER: userIdentifier + userType=PLATFORM_USER
 * - BUSINESS_USER: userIdentifier + userType=BUSINESS_USER + businessId
 */
@Data
public class LoginRequest {

    @NotBlank(message = "User identifier is required")
    private String userIdentifier;

    @NotBlank(message = "Password is required")
    private String password;

    /**
     * User type (REQUIRED) - specifies which user account to authenticate.
     * Since the same username can exist as CUSTOMER, PLATFORM_USER, and BUSINESS_USER,
     * this field is mandatory to disambiguate which account to login to.
     */
    @NotNull(message = "User type is required")
    private UserType userType;

    /**
     * Business ID (REQUIRED for BUSINESS_USER).
     * Specifies which business the user belongs to.
     * Not required for CUSTOMER or PLATFORM_USER.
     */
    private UUID businessId;
}