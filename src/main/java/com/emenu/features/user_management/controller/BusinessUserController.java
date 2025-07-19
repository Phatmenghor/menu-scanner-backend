package com.emenu.features.user_management.controller;

import com.emenu.features.user_management.dto.filter.BusinessUserFilterRequest;
import com.emenu.features.user_management.dto.request.CreateBusinessUserRequest;
import com.emenu.features.user_management.dto.response.BusinessUserResponse;
import com.emenu.features.user_management.dto.update.UpdateBusinessUserRequest;
import com.emenu.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business-users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Business Users", description = "Business user management")
@PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER')")
public class BusinessUserController {

    private final BusinessUserService businessUserService;

    @PostMapping
    @Operation(summary = "Create business user")
    public ResponseEntity<ApiResponse<BusinessUserResponse>> createBusinessUser(
            @Valid @RequestBody CreateBusinessUserRequest request) {
        log.info("Creating business user: {}", request.getEmail());
        BusinessUserResponse response = businessUserService.createBusinessUser(request);
        return ResponseEntity.ok(ApiResponse.success("Business user created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get business user by ID")
    public ResponseEntity<ApiResponse<BusinessUserResponse>> getBusinessUser(@PathVariable UUID id) {
        log.info("Getting business user: {}", id);
        BusinessUserResponse response = businessUserService.getBusinessUser(id);
        return ResponseEntity.ok(ApiResponse.success("Business user retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update business user")
    public ResponseEntity<ApiResponse<BusinessUserResponse>> updateBusinessUser(
            @PathVariable UUID id, @Valid @RequestBody UpdateBusinessUserRequest request) {
        log.info("Updating business user: {}", id);
        BusinessUserResponse response = businessUserService.updateBusinessUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Business user updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete business user")
    public ResponseEntity<ApiResponse<Void>> deleteBusinessUser(@PathVariable UUID id) {
        log.info("Deleting business user: {}", id);
        businessUserService.deleteBusinessUser(id);
        return ResponseEntity.ok(ApiResponse.success("Business user deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "List business users")
    public ResponseEntity<ApiResponse<PaginationResponse<BusinessUserResponse>>> listBusinessUsers(
            @ModelAttribute BusinessUserFilterRequest filter) {
        log.info("Listing business users");
        PaginationResponse<BusinessUserResponse> response = businessUserService.listBusinessUsers(filter);
        return ResponseEntity.ok(ApiResponse.success("Business users retrieved successfully", response));
    }

    @PostMapping("/{id}/change-password")
    @Operation(summary = "Change business user password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable UUID id, @RequestParam String newPassword) {
        log.info("Changing password for business user: {}", id);
        businessUserService.changePassword(id, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}