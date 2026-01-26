package com.emenu.features.auth.dto.update;

import com.emenu.enums.user.UserType;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class RoleUpdateRequest {

    @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
    private String name;

    @Size(max = 200, message = "Display name must not exceed 200 characters")
    private String displayName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Optional business ID for business-specific roles.
     * If null, the role will be a platform-level role.
     */
    private UUID businessId;

    /**
     * The user type this role belongs to.
     * PLATFORM_USER - for platform admin roles
     * BUSINESS_USER - for business-specific roles
     * CUSTOMER - for customer roles
     */
    private UserType userType;
}
