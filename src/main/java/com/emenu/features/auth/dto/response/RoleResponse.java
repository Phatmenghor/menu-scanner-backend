package com.emenu.features.auth.dto.response;

import com.emenu.enums.user.UserType;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleResponse extends BaseAuditResponse {

    private String name;
    private String displayName;
    private String description;
    private UUID businessId;
    private String businessName;

    /**
     * The user type this role belongs to.
     * PLATFORM_USER - for platform admin roles
     * BUSINESS_USER - for business-specific roles
     * CUSTOMER - for customer roles
     */
    private UserType userType;
}
