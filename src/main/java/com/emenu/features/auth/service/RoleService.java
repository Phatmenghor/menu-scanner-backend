package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.RoleFilterRequest;
import com.emenu.features.auth.dto.request.RoleCreateRequest;
import com.emenu.features.auth.dto.response.RoleDetailResponse;
import com.emenu.features.auth.dto.response.RoleResponse;
import com.emenu.features.auth.dto.update.RoleUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    RoleResponse createRole(RoleCreateRequest request);

    PaginationResponse<RoleResponse> getAllRoles(RoleFilterRequest request);

    List<RoleResponse> getAllRolesList(RoleFilterRequest request);

    RoleDetailResponse getRoleById(UUID roleId);

    RoleResponse updateRole(UUID roleId, RoleUpdateRequest request);

    RoleResponse deleteRole(UUID roleId);
}
