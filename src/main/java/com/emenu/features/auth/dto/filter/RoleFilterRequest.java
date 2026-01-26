package com.emenu.features.auth.dto.filter;

import com.emenu.enums.user.UserType;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleFilterRequest extends BaseFilterRequest {

    /**
     * Filter by business ID. If null, returns all roles (platform + business).
     * If specified, returns only roles for that business.
     */
    private UUID businessId;

    /**
     * Filter to only show platform-level roles (businessId is null)
     */
    private Boolean platformRolesOnly;

    /**
     * Filter by role types: PLATFORM, BUSINESS, CUSTOMER
     */
    private List<String> roleTypes;

    /**
     * Filter by user types: PLATFORM_USER, BUSINESS_USER, CUSTOMER
     */
    private List<UserType> userTypes;

    /**
     * Search text for role name, display name, or description
     */
    private String search;
}
