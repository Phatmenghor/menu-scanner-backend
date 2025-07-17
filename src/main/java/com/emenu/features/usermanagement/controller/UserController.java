package com.emenu.features.usermanagement.controller;

import com.emenu.features.usermanagement.dto.filter.UserFilterRequest;
import com.emenu.features.usermanagement.dto.request.ChangePasswordRequest;
import com.emenu.features.usermanagement.dto.request.CreateUserRequest;
import com.emenu.features.usermanagement.dto.response.UserResponse;
import com.emenu.features.usermanagement.dto.response.UserSummaryResponse;
import com.emenu.features.usermanagement.dto.update.UpdateUserRequest;
import com.emenu.features.usermanagement.service.UserService;
import com.emenu.shared.constants.Messages;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User management operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user account")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("REST request to create user with email: {}", request.getEmail());
        
        UserResponse response = userService.createUser(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Messages.USER_CREATED_SUCCESS, response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by ID")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PLATFORM_ADMIN') or @securityUtils.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("REST request to get user by ID: {}", id);
        
        UserResponse response = userService.getUserById(id);
        
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user information")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PLATFORM_ADMIN') or @securityUtils.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("REST request to update user with ID: {}", id);
        
        UserResponse response = userService.updateUser(id, request);
        
        return ResponseEntity.ok(ApiResponse.success(Messages.USER_UPDATED_SUCCESS, response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Soft delete a user account")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("REST request to delete user with ID: {}", id);
        
        userService.deleteUser(id);
        
        return ResponseEntity.ok(ApiResponse.success(Messages.USER_DELETED_SUCCESS, null));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore user", description = "Restore a soft-deleted user account")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> restoreUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("REST request to restore user with ID: {}", id);
        
        userService.restoreUser(id);
        
        return ResponseEntity.ok(ApiResponse.success("User restored successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get users", description = "Retrieve users with filtering and pagination")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<PaginationResponse<UserSummaryResponse>>> getUsers(
            @ModelAttribute UserFilterRequest filter) {
        log.info("REST request to get users with filter: {}", filter);
        
        PaginationResponse<UserSummaryResponse> response = userService.getUsers(filter);
        
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }

    @PostMapping("/{id}/change-password")
    @Operation(summary = "Change password", description = "Change user password")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PLATFORM_ADMIN') or @securityUtils.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> changePassword(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("REST request to change password for user with ID: {}", id);
        
        UserResponse response = userService.changePassword(id, request);
        
        return ResponseEntity.ok(ApiResponse.success(Messages.PASSWORD_CHANGED_SUCCESS, response));
    }

    @PostMapping("/{id}/lock")
    @Operation(summary = "Lock user", description = "Lock user account")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> lockUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("REST request to lock user with ID: {}", id);
        
        userService.lockUser(id);
        
        return ResponseEntity.ok(ApiResponse.success("User locked successfully", null));
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Unlock user", description = "Unlock user account")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unlockUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("REST request to unlock user with ID: {}", id);
        
        userService.unlockUser(id);
        
        return ResponseEntity.ok(ApiResponse.success("User unlocked successfully", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.info("REST request to get current user");
        
        UserResponse response = userService.getCurrentUser();
        
        return ResponseEntity.ok(ApiResponse.success("Current user retrieved successfully", response));
    }

    @PostMapping("/{id}/loyalty-points")
    @Operation(summary = "Add loyalty points", description = "Add loyalty points to user account")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<Void>> addLoyaltyPoints(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Parameter(description = "Points to add") @RequestParam int points) {
        log.info("REST request to add {} loyalty points to user: {}", points, id);
        
        userService.addLoyaltyPoints(id, points);
        
        return ResponseEntity.ok(ApiResponse.success("Loyalty points added successfully", null));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email with token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "Verification token") @RequestParam String token) {
        log.info("REST request to verify email with token: {}", token);
        
        userService.verifyEmail(token);
        
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Request password reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "User email") @RequestParam String email) {
        log.info("REST request to reset password for email: {}", email);
        
        userService.resetPassword(email);
        
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent", null));
    }

    @PostMapping("/reset-password-confirm")
    @Operation(summary = "Confirm password reset", description = "Reset password with token")
    public ResponseEntity<ApiResponse<Void>> resetPasswordWithToken(
            @Parameter(description = "Reset token") @RequestParam String token,
            @Parameter(description = "New password") @RequestParam String newPassword) {
        log.info("REST request to reset password with token: {}", token);
        
        userService.resetPasswordWithToken(token, newPassword);
        
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}