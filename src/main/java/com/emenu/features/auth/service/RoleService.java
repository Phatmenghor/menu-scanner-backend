package com.emenu.features.auth.service;

import com.emenu.enums.user.UserType;
import com.emenu.features.auth.dto.filter.RoleFilterRequest;
import com.emenu.features.auth.dto.request.RoleCreateRequest;
import com.emenu.features.auth.dto.response.RoleResponse;
import com.emenu.features.auth.dto.update.RoleUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    /**
     * Create a new role (platform-level or business-specific)
     */
    RoleResponse createRole(RoleCreateRequest request);

    /**
     * Get all roles with filtering and pagination
     */
    PaginationResponse<RoleResponse> getAllRoles(RoleFilterRequest request);

    /**
     * Get all platform-level roles (no pagination)
     */
    List<RoleResponse> getPlatformRoles();

    /**
     * Get all roles for a specific business
     */
    List<RoleResponse> getBusinessRoles(UUID businessId);

    /**
     * Get all roles by user type
     */
    List<RoleResponse> getRolesByUserType(UserType userType);

    /**
     * Get all roles by user type for a specific business
     */
    List<RoleResponse> getRolesByUserTypeAndBusinessId(UserType userType, UUID businessId);

    /**
     * Get a role by ID
     */
    RoleResponse getRoleById(UUID roleId);

    /**
     * Get a role by name (platform-level)
     */
    RoleResponse getRoleByName(String name);

    /**
     * Get a role by name for a specific business
     */
    RoleResponse getRoleByNameAndBusinessId(String name, UUID businessId);

    /**
     * Update a role
     */
    RoleResponse updateRole(UUID roleId, RoleUpdateRequest request);

    /**
     * Delete a role (soft delete)
     */
    RoleResponse deleteRole(UUID roleId);

    /**
     * Check if a role exists by name
     */
    boolean existsByName(String name);

    /**
     * Check if a role exists by name and business ID
     */
    boolean existsByNameAndBusinessId(String name, UUID businessId);
}
