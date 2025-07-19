package com.emenu.features.user_management.controller;

import com.emenu.features.user_management.dto.filter.UserFilterRequest;
import com.emenu.features.user_management.dto.request.PasswordChangeRequest;
import com.emenu.features.user_management.dto.request.UserCreateRequest;
import com.emenu.features.user_management.dto.response.UserResponse;
import com.emenu.features.user_management.dto.response.UserSummaryResponse;
import com.emenu.features.user_management.dto.update.UserUpdateRequest;
import com.emenu.features.user_management.service.CustomerService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "Customer management operations")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

    @PostMapping("/register")
    @Operation(summary = "Register customer", description = "Register a new customer")
    public ResponseEntity<ApiResponse<UserResponse>> registerCustomer(
            @Valid @RequestBody UserCreateRequest request) {
        log.info("REST request to register customer: {}", request.getEmail());
        
        UserResponse customer = customerService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer registered successfully", customer));
    }

    @GetMapping
    @Operation(summary = "Get customers", description = "Get paginated list of customers")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<PaginationResponse<UserSummaryResponse>>> getCustomers(
            @ModelAttribute UserFilterRequest filter) {
        log.info("REST request to get customers");
        
        PaginationResponse<UserSummaryResponse> customers = customerService.getCustomers(filter);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Get customer details by ID")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER', 'BUSINESS_MANAGER') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> getCustomerById(
            @Parameter(description = "Customer ID") @PathVariable UUID id) {
        log.info("REST request to get customer by ID: {}", id);
        
        UserResponse customer = customerService.getCustomerProfile(id);
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", customer));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Update customer details")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER', 'BUSINESS_MANAGER') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> updateCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("REST request to update customer: {}", id);
        
        UserResponse customer = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", customer));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "Delete customer")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID id) {
        log.info("REST request to delete customer: {}", id);
        
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }

    @PostMapping("/{id}/loyalty-points")
    @Operation(summary = "Add loyalty points", description = "Add loyalty points to customer")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> addLoyaltyPoints(
            @Parameter(description = "Customer ID") @PathVariable UUID id,
            @Parameter(description = "Points to add") @RequestParam Integer points) {
        log.info("REST request to add {} loyalty points to customer: {}", points, id);
        
        customerService.addLoyaltyPoints(id, points);
        return ResponseEntity.ok(ApiResponse.success("Loyalty points added successfully", null));
    }

    @PostMapping("/{id}/upgrade-tier")
    @Operation(summary = "Upgrade customer tier", description = "Upgrade customer tier based on points")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> upgradeTier(
            @Parameter(description = "Customer ID") @PathVariable UUID id) {
        log.info("REST request to upgrade tier for customer: {}", id);
        
        customerService.upgradeTier(id);
        return ResponseEntity.ok(ApiResponse.success("Customer tier upgraded successfully", null));
    }

    // Customer self-service endpoints
    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Get current customer profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        log.info("REST request to get my customer profile");
        
        UUID currentUserId = securityUtils.getCurrentUserId();
        UserResponse customer = customerService.getCustomerProfile(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", customer));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my profile", description = "Update current customer profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("REST request to update my customer profile");
        
        UUID currentUserId = securityUtils.getCurrentUserId();
        UserResponse customer = customerService.updateCustomer(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", customer));
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Change my password", description = "Change current customer password")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("REST request to change my password");
        
        UUID currentUserId = securityUtils.getCurrentUserId();
        userService.changePassword(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete my account", description = "Delete current customer account")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount() {
        log.info("REST request to delete my customer account");
        
        UUID currentUserId = securityUtils.getCurrentUserId();
        customerService.deleteCustomer(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }
}
