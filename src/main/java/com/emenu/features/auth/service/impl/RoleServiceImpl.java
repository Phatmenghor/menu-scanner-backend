package com.emenu.features.auth.service.impl;

import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.RoleFilterRequest;
import com.emenu.features.auth.dto.request.RoleCreateRequest;
import com.emenu.features.auth.dto.response.RoleDetailResponse;
import com.emenu.features.auth.dto.response.RoleResponse;
import com.emenu.features.auth.dto.update.RoleUpdateRequest;
import com.emenu.features.auth.mapper.RoleMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.service.RoleService;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final BusinessRepository businessRepository;
    private final RoleMapper roleMapper;

    @Override
    public RoleResponse createRole(RoleCreateRequest request) {
        log.info("Creating role: {}", request.getName());

        // Normalize role name to uppercase
        String normalizedName = request.getName().toUpperCase().replace(" ", "_");

        // Check for duplicate role
        if (request.getBusinessId() != null) {
            // Business-specific role
            if (roleRepository.existsByNameAndBusinessIdAndIsDeletedFalse(normalizedName, request.getBusinessId())) {
                throw new ValidationException("Role with this name already exists for this business");
            }
            // Validate business exists
            businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                    .orElseThrow(() -> new ValidationException("Business not found"));
        } else {
            // Platform-level role
            if (roleRepository.existsByNameAndBusinessIdIsNullAndIsDeletedFalse(normalizedName)) {
                throw new ValidationException("Platform role with this name already exists");
            }
        }

        Role role = roleMapper.toEntity(request);
        role.setName(normalizedName);

        // Set display name if not provided
        if (role.getDisplayName() == null || role.getDisplayName().isEmpty()) {
            role.setDisplayName(normalizedName.replace("_", " "));
        }

        Role savedRole = roleRepository.save(role);
        log.info("Role created: {} with ID: {}", savedRole.getName(), savedRole.getId());

        return roleMapper.toResponse(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<RoleResponse> getAllRoles(RoleFilterRequest request) {
        log.debug("Getting all roles with filters and pagination");

        Pageable pageable = PaginationUtils.createPageable(
                request.getPageNo(),
                request.getPageSize(),
                request.getSortBy(),
                request.getSortDirection()
        );

        // Convert empty list to null to skip filtering
        List<UserType> userTypes = (request.getUserTypes() != null && !request.getUserTypes().isEmpty())
                ? request.getUserTypes() : null;

        Boolean includeAll = request.getIncludeAll() != null ? request.getIncludeAll() : false;

        Page<Role> rolesPage = roleRepository.findAllWithFilters(
                request.getBusinessId(),
                userTypes,
                request.getSearch(),
                includeAll,
                pageable
        );

        List<RoleResponse> responses = rolesPage.getContent().stream()
                .map(roleMapper::toResponse)
                .toList();

        return PaginationResponse.<RoleResponse>builder()
                .content(responses)
                .pageNo(rolesPage.getNumber() + 1)
                .pageSize(rolesPage.getSize())
                .totalElements(rolesPage.getTotalElements())
                .totalPages(rolesPage.getTotalPages())
                .last(rolesPage.isLast())
                .first(rolesPage.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRolesList(RoleFilterRequest request) {
        log.debug("Getting all roles as list with filters");

        // Convert empty list to null to skip filtering
        List<UserType> userTypes = (request.getUserTypes() != null && !request.getUserTypes().isEmpty())
                ? request.getUserTypes() : null;

        Boolean includeAll = request.getIncludeAll() != null ? request.getIncludeAll() : false;

        List<Role> roles = roleRepository.findAllListWithFilters(
                request.getBusinessId(),
                userTypes,
                request.getSearch(),
                includeAll
        );

        return roles.stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetailResponse getRoleById(UUID roleId) {
        log.debug("Getting role by ID: {}", roleId);
        Role role = roleRepository.findByIdAndIsDeletedFalse(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        RoleDetailResponse response = roleMapper.toDetailResponse(role);
        if (role.getBusinessId() != null) {
            businessRepository.findByIdAndIsDeletedFalse(role.getBusinessId())
                    .ifPresent(business -> response.setBusinessName(business.getName()));
        }
        return response;
    }

    @Override
    public RoleResponse updateRole(UUID roleId, RoleUpdateRequest request) {
        log.info("Updating role: {}", roleId);

        Role role = roleRepository.findByIdAndIsDeletedFalse(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        // Check if trying to update a system role
        if (isSystemRole(role.getName())) {
            throw new ValidationException("Cannot modify system roles");
        }

        // Validate name uniqueness if changing name
        if (request.getName() != null && !request.getName().isEmpty()) {
            String normalizedName = request.getName().toUpperCase().replace(" ", "_");
            if (!normalizedName.equals(role.getName())) {
                if (role.getBusinessId() != null) {
                    if (roleRepository.existsByNameAndBusinessIdAndIsDeletedFalse(normalizedName, role.getBusinessId())) {
                        throw new ValidationException("Role with this name already exists for this business");
                    }
                } else {
                    if (roleRepository.existsByNameAndBusinessIdIsNullAndIsDeletedFalse(normalizedName)) {
                        throw new ValidationException("Platform role with this name already exists");
                    }
                }
                role.setName(normalizedName);
            }
        }

        roleMapper.updateEntity(request, role);

        // Update display name if name was updated but display name wasn't
        if (request.getName() != null && request.getDisplayName() == null) {
            role.setDisplayName(role.getName().replace("_", " "));
        }

        Role savedRole = roleRepository.save(role);
        log.info("Role updated: {}", savedRole.getName());

        return roleMapper.toResponse(savedRole);
    }

    @Override
    public RoleResponse deleteRole(UUID roleId) {
        log.info("Deleting role: {}", roleId);

        Role role = roleRepository.findByIdAndIsDeletedFalse(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        // Check if trying to delete a system role
        if (isSystemRole(role.getName())) {
            throw new ValidationException("Cannot delete system roles");
        }

        // Soft delete - does not affect current users
        // Owners can update users themselves if needed
        role.setIsDeleted(true);
        role.setDeletedAt(LocalDateTime.now());
        Role deletedRole = roleRepository.save(role);

        log.info("Role soft deleted: {}", deletedRole.getName());
        return roleMapper.toResponse(deletedRole);
    }

    private boolean isSystemRole(String roleName) {
        return List.of("PLATFORM_OWNER", "BUSINESS_OWNER", "CUSTOMER").contains(roleName);
    }
}
