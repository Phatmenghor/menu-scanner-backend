package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.AdminPasswordResetRequest;
import com.emenu.features.auth.dto.request.BusinessOwnerCreateRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.response.BusinessOwnerCreateResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    // ================================
    // CURRENT USER PROFILE OPERATIONS
    // ================================

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.info("Getting current user profile");
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating current user profile");
        UserResponse response = userService.updateCurrentUser(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    // ================================
    // USER CRUD OPERATIONS
    // ================================

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getAllUsers(
            @Valid @RequestBody UserFilterRequest request) {
        log.info("Getting all users with filters - UserType: {}, AccountStatus: {}, Search: {}",
                request.getUserType(), request.getAccountStatus(), request.getSearch());
        PaginationResponse<UserResponse> response = userService.getAllUsers(request);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID userId) {
        log.info("Getting user by ID: {}", userId);
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        log.info("Creating new user: {} with type: {}", request.getEmail(), request.getUserType());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", response));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user: {}", userId);
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> deleteUser(@PathVariable UUID userId) {
        log.info("Deleting user: {}", userId);
        UserResponse userResponse = userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", userResponse));
    }

    // ================================
    // BUSINESS OWNER CREATION
    // ================================

    @PostMapping("/business-owner")
    public ResponseEntity<ApiResponse<BusinessOwnerCreateResponse>> createBusinessOwner(
            @Valid @RequestBody BusinessOwnerCreateRequest request) {
        log.info("Creating comprehensive business owner with business: {} for userIdentifier: {}",
                request.getBusinessName(), request.getOwnerUserIdentifier());

        BusinessOwnerCreateResponse response = userService.createBusinessOwner(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business owner created successfully with full setup", response));
    }

    // ================================
    // PASSWORD MANAGEMENT
    // ================================

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<UserResponse>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("Password change request received");
        UserResponse response = authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", response));
    }

    @PostMapping("/admin/reset-password")
    public ResponseEntity<ApiResponse<UserResponse>> adminResetPassword(
            @Valid @RequestBody AdminPasswordResetRequest request) {
        log.info("Admin password reset request for user: {}", request.getUserId());
        UserResponse response = authService.adminResetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", response));
    }

    // ================================
    // BUSINESS USER SPECIFIC ENDPOINTS
    // ================================

    @PostMapping("/my-business/all")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getMyBusinessUsers(
            @Valid @RequestBody UserFilterRequest request) {
        log.info("Getting users from current user's business");
        // The service will automatically filter by current user's business
        PaginationResponse<UserResponse> response = userService.getAllUsers(request);
        return ResponseEntity.ok(ApiResponse.success("Business users retrieved successfully", response));
    }

    @PostMapping("/my-business/create")
    public ResponseEntity<ApiResponse<UserResponse>> createUserForMyBusiness(
            @Valid @RequestBody UserCreateRequest request) {
        log.info("Creating user for current user's business: {}", request.getEmail());

        // The service will automatically validate and assign to current user's business
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully for your business", response));
    }
}