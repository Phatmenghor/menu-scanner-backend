package com.emenu.features.auth.service.impl;

import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.RoleFilterRequest;
import com.emenu.features.auth.dto.request.RoleCreateRequest;
import com.emenu.features.auth.dto.response.RoleResponse;
import com.emenu.features.auth.dto.update.RoleUpdateRequest;
import com.emenu.features.auth.mapper.RoleMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.service.RoleService;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
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
    private final PaginationMapper paginationMapper;

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
            Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
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

        return enrichRoleResponse(roleMapper.toResponse(savedRole), request.getBusinessId());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<RoleResponse> getAllRoles(RoleFilterRequest request) {
        log.debug("Getting all roles with filters");

        Pageable pageable = PaginationUtils.createPageable(
                request.getPageNo(),
                request.getPageSize(),
                request.getSortBy(),
                request.getSortDirection()
        );

        // Convert empty list to null to skip filtering
        List<UserType> userTypes = (request.getUserTypes() != null && !request.getUserTypes().isEmpty())
                ? request.getUserTypes() : null;

        Page<Role> rolesPage = roleRepository.findAllWithFilters(
                request.getBusinessId(),
                request.getPlatformRolesOnly(),
                userTypes,
                request.getSearch(),
                pageable
        );

        List<RoleResponse> responses = rolesPage.getContent().stream()
                .map(roleMapper::toResponse)
                .map(response -> enrichRoleResponse(response, response.getBusinessId()))
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
    public List<RoleResponse> getPlatformRoles() {
        log.debug("Getting all platform roles");
        List<Role> roles = roleRepository.findByBusinessIdIsNullAndIsDeletedFalse();
        return roles.stream()
                .map(roleMapper::toResponse)
                .map(response -> enrichRoleResponse(response, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getBusinessRoles(UUID businessId) {
        log.debug("Getting roles for business: {}", businessId);

        // Validate business exists
        if (!businessRepository.existsByIdAndIsDeletedFalse(businessId)) {
            throw new ResourceNotFoundException("Business not found");
        }

        List<Role> roles = roleRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        return roles.stream()
                .map(roleMapper::toResponse)
                .map(response -> enrichRoleResponse(response, businessId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRolesByUserType(UserType userType) {
        log.debug("Getting roles for user type: {}", userType);
        List<Role> roles = roleRepository.findByUserTypeAndIsDeletedFalse(userType);
        return roles.stream()
                .map(roleMapper::toResponse)
                .map(response -> enrichRoleResponse(response, response.getBusinessId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRolesByUserTypeAndBusinessId(UserType userType, UUID businessId) {
        log.debug("Getting roles for user type: {} and business: {}", userType, businessId);

        // Validate business exists
        if (!businessRepository.existsByIdAndIsDeletedFalse(businessId)) {
            throw new ResourceNotFoundException("Business not found");
        }

        List<Role> roles = roleRepository.findByUserTypeAndBusinessIdAndIsDeletedFalse(userType, businessId);
        return roles.stream()
                .map(roleMapper::toResponse)
                .map(response -> enrichRoleResponse(response, businessId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID roleId) {
        log.debug("Getting role by ID: {}", roleId);
        Role role = roleRepository.findByIdAndIsDeletedFalse(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        return enrichRoleResponse(roleMapper.toResponse(role), role.getBusinessId());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(String name) {
        log.debug("Getting role by name: {}", name);
        Role role = roleRepository.findByNameAndBusinessIdIsNullAndIsDeletedFalse(name.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        return enrichRoleResponse(roleMapper.toResponse(role), null);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleByNameAndBusinessId(String name, UUID businessId) {
        log.debug("Getting role by name: {} and business: {}", name, businessId);
        Role role = roleRepository.findByNameAndBusinessIdAndIsDeletedFalse(name.toUpperCase(), businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        return enrichRoleResponse(roleMapper.toResponse(role), businessId);
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

        return enrichRoleResponse(roleMapper.toResponse(savedRole), savedRole.getBusinessId());
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

        // Check if role has users assigned
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            throw new ValidationException("Cannot delete role with assigned users. Remove all users first.");
        }

        role.setIsDeleted(true);
        role.setDeletedAt(LocalDateTime.now());
        Role deletedRole = roleRepository.save(role);

        log.info("Role deleted: {}", deletedRole.getName());
        return enrichRoleResponse(roleMapper.toResponse(deletedRole), deletedRole.getBusinessId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return roleRepository.existsByNameAndIsDeletedFalse(name.toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndBusinessId(String name, UUID businessId) {
        return roleRepository.existsByNameAndBusinessIdAndIsDeletedFalse(name.toUpperCase(), businessId);
    }

    /**
     * Check if role is a system role that should not be modified
     */
    private boolean isSystemRole(String roleName) {
        return List.of("PLATFORM_OWNER", "BUSINESS_OWNER", "CUSTOMER").contains(roleName);
    }

    /**
     * Enrich role response with business name
     */
    private RoleResponse enrichRoleResponse(RoleResponse response, UUID businessId) {
        if (businessId != null) {
            businessRepository.findByIdAndIsDeletedFalse(businessId)
                    .ifPresent(business -> response.setBusinessName(business.getName()));
        }
        return response;
    }
}
