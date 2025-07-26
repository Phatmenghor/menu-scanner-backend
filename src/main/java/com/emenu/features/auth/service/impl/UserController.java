package com.emenu.features.auth.service.impl;

import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.AdminPasswordResetRequest;
import com.emenu.features.auth.dto.request.BusinessUserCreateRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.service.AuthService;
import com.emenu.features.auth.service.UserService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
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
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.info("Getting current user profile");
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", response));
    }

    /**
     * Update current user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating current user profile");
        UserResponse response = userService.updateCurrentUser(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    /**
     * Get all users with filtering and pagination
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getAllUsers(@Valid @RequestBody UserFilterRequest request) {
        log.info("Getting all users with filters");
        PaginationResponse<UserResponse> response = userService.getAllUsers(request);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID userId) {
        log.info("Getting user by ID: {}", userId);
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    /**
     * Create regular user (Platform and Business staff only - no business creation)
     */
    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Creating new user: {}", request.getEmail());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", response));
    }

    /**
     * ✅ NEW: Create business user with business and subdomain (Platform admins only)
     */
    @PostMapping("/business-user")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createBusinessUser(@Valid @RequestBody BusinessUserCreateRequest request) {
        log.info("Creating business user with business: {} for email: {}", request.getBusinessName(), request.getEmail());
        UserResponse response = userService.createBusinessUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business user created successfully with business and subdomain", response));
    }

    /**
     * Update user
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable UUID userId,
                                                                @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user: {}", userId);
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<UserResponse>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.info("Password change request received");
        UserResponse response = authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", response));
    }

    /**
     * Admin reset password
     */
    @PostMapping("/admin/reset-password")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> adminResetPassword(@Valid @RequestBody AdminPasswordResetRequest request) {
        log.info("Admin password reset request for user: {}", request.getUserId());
        UserResponse response = authService.adminResetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", response));
    }

    /**
     * Delete user (soft delete)
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> deleteUser(@PathVariable UUID userId) {
        log.info("Deleting user: {}", userId);
        UserResponse userResponse = userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", userResponse));
    }
}