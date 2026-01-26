package com.emenu.features.auth.service;

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
     * Get all roles as list with filtering (no pagination)
     */
    List<RoleResponse> getAllRolesList(RoleFilterRequest request);

    /**
     * Get a role by ID
     */
    RoleResponse getRoleById(UUID roleId);

    /**
     * Update a role
     */
    RoleResponse updateRole(UUID roleId, RoleUpdateRequest request);

    /**
     * Delete a role (soft delete)
     * Does not affect current users - owners can update users themselves
     */
    RoleResponse deleteRole(UUID roleId);
}
