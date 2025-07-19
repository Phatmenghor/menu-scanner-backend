package com.emenu.features.user_management.controller;

import com.emenu.features.user_management.dto.filter.UserFilterRequest;
import com.emenu.features.user_management.dto.request.PasswordChangeRequest;
import com.emenu.features.user_management.dto.request.UserCreateRequest;
import com.emenu.features.user_management.dto.response.UserResponse;
import com.emenu.features.user_management.dto.response.UserSummaryResponse;
import com.emenu.features.user_management.dto.update.UserUpdateRequest;
import com.emenu.features.user_management.service.PlatformUserService;
import com.emenu.features.user_management.service.UserService;
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
@RequestMapping("/api/v1/platform/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Platform User Management", description = "Platform user management operations")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
public class PlatformUserController {

    private final PlatformUserService platformUserService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create platform user", description = "Create a new platform user")
    public ResponseEntity<ApiResponse<UserResponse>> createPlatformUser(
            @Valid @RequestBody UserCreateRequest request) {
        log.info("REST request to create platform user: {}", request.getEmail());
        
        UserResponse user = platformUserService.createPlatformUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Platform user created successfully", user));
    }

    @GetMapping
    @Operation(summary = "Get all platform users", description = "Get paginated list of platform users")
    public ResponseEntity<ApiResponse<PaginationResponse<UserSummaryResponse>>> getAllPlatformUsers(
            @ModelAttribute UserFilterRequest filter) {
        log.info("REST request to get platform users with filter: {}", filter);
        
        PaginationResponse<UserSummaryResponse> users = platformUserService.getAllPlatformUsers(filter);
        return ResponseEntity.ok(ApiResponse.success("Platform users retrieved successfully", users));
    }

    @GetMapping("/business-users")
    @Operation(summary = "Get all business users", description = "Get paginated list of all business users")
    public ResponseEntity<ApiResponse<PaginationResponse<UserSummaryResponse>>> getAllBusinessUsers(
            @ModelAttribute UserFilterRequest filter) {
        log.info("REST request to get all business users");
        
        PaginationResponse<UserSummaryResponse> users = platformUserService.getAllBusinessUsers(filter);
        return ResponseEntity.ok(ApiResponse.success("Business users retrieved successfully", users));
    }

    @GetMapping("/customers")
    @Operation(summary = "Get all customers", description = "Get paginated list of all customers")
    public ResponseEntity<ApiResponse<PaginationResponse<UserSummaryResponse>>> getAllCustomers(
            @ModelAttribute UserFilterRequest filter) {
        log.info("REST request to get all customers");
        
        PaginationResponse<UserSummaryResponse> users = platformUserService.getAllCustomers(filter);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", users));
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
    @Operation(summary = "Update platform user", description = "Update platform user details")
    public ResponseEntity<ApiResponse<UserResponse>> updatePlatformUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("REST request to update platform user: {}", id);
        
        UserResponse user = platformUserService.updatePlatformUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Platform user updated successfully", user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete platform user", description = "Delete platform user")
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deletePlatformUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("REST request to delete platform user: {}", id);
        
        platformUserService.deletePlatformUser(id);
        return ResponseEntity.ok(ApiResponse.success("Platform user deleted successfully", null));
    }

    @PostMapping("/{id}/force-password-reset")
    @Operation(summary = "Force password reset", description = "Force password reset for any user")
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<Void>> forcePasswordReset(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("REST request to force password reset for user: {}", id);
        
        platformUserService.forcePasswordReset(id);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    @PostMapping("/{id}/lock")
    @Operation(summary = "Lock user", description = "Lock user account")
    public ResponseEntity<ApiResponse<Void>> lockUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("REST request to lock user: {}", id);
        
        platformUserService.lockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User locked successfully", null));
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Unlock user", description = "Unlock user account")
    public ResponseEntity<ApiResponse<Void>> unlockUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("REST request to unlock user: {}", id);
        
        platformUserService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User unlocked successfully", null));
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
    @Operation(summary = "Change user password", description = "Change password for any user")
    public ResponseEntity<ApiResponse<Void>> changeUserPassword(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("REST request to change password for user: {}", id);
        
        userService.changePassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics", description = "Get user statistics for dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics() {
        log.info("REST request to get user statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", platformUserService.getTotalUsers());
        stats.put("platformUsers", platformUserService.getPlatformUsersCount());
        stats.put("businessUsers", platformUserService.getBusinessUsersCount());
        stats.put("customers", platformUserService.getCustomersCount());
        
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
    }
}