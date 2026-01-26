package com.emenu.features.auth.controller;

import com.emenu.enums.user.UserType;
import com.emenu.features.auth.dto.filter.RoleFilterRequest;
import com.emenu.features.auth.dto.request.RoleCreateRequest;
import com.emenu.features.auth.dto.response.RoleResponse;
import com.emenu.features.auth.dto.update.RoleUpdateRequest;
import com.emenu.features.auth.service.RoleService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Role Management Controller
 * Provides endpoints for dynamic role creation and management
 * for platform, business, and customer roles.
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;

    /**
     * Create a new role (platform-level or business-specific)
     * Platform Owner can create platform roles
     * Business Owner can create business-specific roles
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody RoleCreateRequest request) {
        log.info("Create role request: {}", request.getName());
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", response));
    }

    /**
     * Get all roles with filtering and pagination
     * Platform Owner sees all roles
     * Business Owner sees platform roles + their business roles
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<RoleResponse>>> getAllRoles(
            @Valid @RequestBody RoleFilterRequest request) {
        log.info("Get all roles with filters");
        PaginationResponse<RoleResponse> response = roleService.getAllRoles(request);
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", response));
    }

    /**
     * Get all platform-level roles (no pagination)
     * Available to all authenticated users
     */
    @GetMapping("/platform")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getPlatformRoles() {
        log.info("Get platform roles");
        List<RoleResponse> response = roleService.getPlatformRoles();
        return ResponseEntity.ok(ApiResponse.success("Platform roles retrieved", response));
    }

    /**
     * Get all roles for a specific business
     */
    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getBusinessRoles(
            @PathVariable UUID businessId) {
        log.info("Get roles for business: {}", businessId);
        List<RoleResponse> response = roleService.getBusinessRoles(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business roles retrieved", response));
    }

    /**
     * Get all roles by user type
     * PLATFORM_USER - for platform admin roles
     * BUSINESS_USER - for business-specific roles
     * CUSTOMER - for customer roles
     */
    @GetMapping("/user-type/{userType}")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRolesByUserType(
            @PathVariable UserType userType) {
        log.info("Get roles for user type: {}", userType);
        List<RoleResponse> response = roleService.getRolesByUserType(userType);
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved for user type", response));
    }

    /**
     * Get all roles by user type for a specific business
     */
    @GetMapping("/user-type/{userType}/business/{businessId}")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRolesByUserTypeAndBusinessId(
            @PathVariable UserType userType,
            @PathVariable UUID businessId) {
        log.info("Get roles for user type: {} and business: {}", userType, businessId);
        List<RoleResponse> response = roleService.getRolesByUserTypeAndBusinessId(userType, businessId);
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved for user type and business", response));
    }

    /**
     * Get a role by ID
     */
    @GetMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(
            @PathVariable UUID roleId) {
        log.info("Get role by ID: {}", roleId);
        RoleResponse response = roleService.getRoleById(roleId);
        return ResponseEntity.ok(ApiResponse.success("Role retrieved", response));
    }

    /**
     * Get a platform role by name
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(
            @PathVariable String name) {
        log.info("Get role by name: {}", name);
        RoleResponse response = roleService.getRoleByName(name);
        return ResponseEntity.ok(ApiResponse.success("Role retrieved", response));
    }

    /**
     * Get a business-specific role by name and business ID
     */
    @GetMapping("/business/{businessId}/name/{name}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'BUSINESS_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByNameAndBusinessId(
            @PathVariable UUID businessId,
            @PathVariable String name) {
        log.info("Get role by name: {} and business: {}", name, businessId);
        RoleResponse response = roleService.getRoleByNameAndBusinessId(name, businessId);
        return ResponseEntity.ok(ApiResponse.success("Role retrieved", response));
    }

    /**
     * Update a role
     * Cannot update system roles (PLATFORM_OWNER, BUSINESS_OWNER, CUSTOMER)
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody RoleUpdateRequest request) {
        log.info("Update role: {}", roleId);
        RoleResponse response = roleService.updateRole(roleId, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", response));
    }

    /**
     * Delete a role (soft delete)
     * Cannot delete system roles
     * Cannot delete roles with assigned users
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<RoleResponse>> deleteRole(
            @PathVariable UUID roleId) {
        log.info("Delete role: {}", roleId);
        RoleResponse response = roleService.deleteRole(roleId);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", response));
    }

    /**
     * Check if a platform role exists by name
     */
    @GetMapping("/exists/{name}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'BUSINESS_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> existsByName(
            @PathVariable String name) {
        log.info("Check if role exists: {}", name);
        boolean exists = roleService.existsByName(name);
        return ResponseEntity.ok(ApiResponse.success("Role existence checked", exists));
    }

    /**
     * Check if a business-specific role exists
     */
    @GetMapping("/exists/business/{businessId}/name/{name}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'BUSINESS_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> existsByNameAndBusinessId(
            @PathVariable UUID businessId,
            @PathVariable String name) {
        log.info("Check if role exists: {} for business: {}", name, businessId);
        boolean exists = roleService.existsByNameAndBusinessId(name, businessId);
        return ResponseEntity.ok(ApiResponse.success("Role existence checked", exists));
    }
}
