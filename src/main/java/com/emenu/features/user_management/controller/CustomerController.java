package com.emenu.features.user_management.controller;

import com.emenu.features.user_management.dto.filter.CustomerFilterRequest;
import com.emenu.features.user_management.dto.request.CreateCustomerRequest;
import com.emenu.features.user_management.dto.response.CustomerResponse;
import com.emenu.features.user_management.dto.update.UpdateCustomerRequest;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customers", description = "Customer management")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create customer")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        log.info("Creating customer: {}", request.getEmail());
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.ok(ApiResponse.success("Customer created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER', 'BUSINESS_MANAGER') or @securityUtils.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable UUID id) {
        log.info("Getting customer: {}", id);
        CustomerResponse response = customerService.getCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER', 'BUSINESS_MANAGER') or @securityUtils.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest request) {
        log.info("Updating customer: {}", id);
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", response));
    }

    @GetMapping
    @Operation(summary = "List customers")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<PaginationResponse<CustomerResponse>>> listCustomers(
            @ModelAttribute CustomerFilterRequest filter) {
        log.info("Listing customers");
        PaginationResponse<CustomerResponse> response = customerService.listCustomers(filter);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", response));
    }

    @PostMapping("/{id}/loyalty-points")
    @Operation(summary = "Add loyalty points")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'BUSINESS_OWNER', 'BUSINESS_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> addLoyaltyPoints(
            @PathVariable UUID id, @RequestParam Integer points) {
        log.info("Adding {} loyalty points to customer: {}", points, id);
        customerService.addLoyaltyPoints(id, points);
        return ResponseEntity.ok(ApiResponse.success("Loyalty points added successfully", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current customer profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCurrentCustomer() {
        log.info("Getting current customer profile");
        CustomerResponse response = customerService.getCurrentCustomer();
        return ResponseEntity.ok(ApiResponse.success("Customer profile retrieved successfully", response));
    }
}