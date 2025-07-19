package com.emenu.features.user_management.controller;

import com.emenu.features.user_management.dto.filter.UserFilterRequest;
import com.emenu.features.user_management.dto.request.PasswordChangeRequest;
import com.emenu.features.user_management.dto.request.UserCreateRequest;
import com.emenu.features.user_management.dto.response.UserResponse;
import com.emenu.features.user_management.dto.response.UserSummaryResponse;
import com.emenu.features.user_management.dto.update.UserUpdateRequest;
import com.emenu.features.user_management.service.BusinessUserService;
import com.emenu.features.user_management.service.UserService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Business User Management", description = "Business user management operations")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'BUSINESS_MANAGER')")
public class BusinessUserController {

    private final BusinessUserService businessUserService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Create business user", description = "Create a new business user")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<UserResponse>> createBusinessUser(
            @Valid @RequestBody UserCreateRequest request) {
        log.info("REST request to create business user: {}", request.getEmail());
        
        UserResponse user = businessUserService.createBusinessUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business user created successfully", user));
    }

    @PostMapping("/staff")
    @Operation(summary = "Create staff member", description = "Create a new staff member for current business")
    public ResponseEntity<ApiResponse<UserResponse>> createStaffMember(
            @Valid @RequestBody UserCreateRequest request) {
        log.info("REST request to create staff member: {}", request.getEmail());
        
        UserResponse user = businessUserService.createStaffMember(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff member created successfully", user));
    }

    @GetMapping
    @Operation(summary = "Get my business users", description = "Get users from current user's business")
    public ResponseEntity<ApiResponse<PaginationResponse<UserSummaryResponse>>> getMyBusinessUsers(
            @ModelAttribute UserFilterRequest filter) {
        log.info("REST request to get my business users");
        
        PaginationResponse<UserSummaryResponse> users = businessUserService.getMyBusinessUsers(filter);
        return ResponseEntity.ok(ApiResponse.success("Business users retrieved successfully", users));
    }

    @GetMapping("/business/{businessId}")
    @Operation(summary = "Get business users by business ID", description = "Get users from specific business")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<PaginationResponse<UserSummaryResponse>>> getBusinessUsers(
            @Parameter(description = "Business ID") @PathVariable UUID businessId,
            @ModelAttribute UserFilterRequest filter) {
        log.info("REST request to get users for business: {}", businessId);
        
        PaginationResponse<UserSummaryResponse> users = businessUserService.getBusinessUsers(businessId, filter);
        return ResponseEntity.ok(ApiResponse.success("Business users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("REST request to get user by ID: {}", id);
        
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update business user", description = "Update business user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateBusinessUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("REST request to update business user: {}", id);
        
        UserResponse user = businessUserService.updateBusinessUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Business user updated successfully", user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete business user", description = "Delete business user")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteBusinessUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("REST request to delete business user: {}", id);
        
        businessUserService.deleteBusinessUser(id);
        return ResponseEntity.ok(ApiResponse.success("Business user deleted successfully", null));
    }

    @PostMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle user status", description = "Toggle user active/inactive status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("REST request to toggle status for user: {}", id);
        
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status toggled successfully", null));
    }

    @PostMapping("/{id}/change-password")
    @Operation(summary = "Change user password", description = "Change password for business user")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<Void>> changeUserPassword(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("REST request to change password for user: {}", id);
        
        userService.changePassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        log.info("REST request to get my profile");
        
        UUID currentUserId = securityUtils.getCurrentUserId();
        UserResponse user = userService.getUserById(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my profile", description = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("REST request to update my profile");
        
        UUID currentUserId = securityUtils.getCurrentUserId();
        UserResponse user = userService.updateUser(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Change my password", description = "Change current user password")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("REST request to change my password");
        
        UUID currentUserId = securityUtils.getCurrentUserId();
        userService.changePassword(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get business user statistics", description = "Get user statistics for current business")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBusinessUserStatistics() {
        log.info("REST request to get business user statistics");
        
        UUID currentUserId = securityUtils.getCurrentUserId();
        var currentUser = userService.getUserById(currentUserId);
        
        Map<String, Object> stats = new HashMap<>();
        if (currentUser.getBusinessId() != null) {
            stats.put("totalBusinessUsers", businessUserService.getBusinessUsersCount(currentUser.getBusinessId()));
            stats.put("businessId", currentUser.getBusinessId());
            stats.put("businessName", currentUser.getBusinessName());
        } else {
            stats.put("totalBusinessUsers", 0);
            stats.put("businessId", null);
            stats.put("businessName", null);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
    }
}
