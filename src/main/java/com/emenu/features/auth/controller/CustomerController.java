package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.request.CustomerCreateRequest;
import com.emenu.features.auth.dto.response.CustomerResponse;
import com.emenu.features.auth.dto.update.CustomerUpdateRequest;
import com.emenu.features.auth.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerCreateRequest request) {
        log.info("Creating customer: {}", request.getEmail());
        CustomerResponse customer = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", customer));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<CustomerResponse>>> getCustomers(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        log.info("Getting customers");
        PaginationResponse<CustomerResponse> customers = customerService.getCustomers(pageNo, pageSize, search);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable UUID id) {
        log.info("Getting customer by ID: {}", id);
        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", customer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerUpdateRequest request) {
        log.info("Updating customer: {}", id);
        CustomerResponse customer = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable UUID id) {
        log.info("Deleting customer: {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }

    // Customer Self-Service (Simplified)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> getMyProfile() {
        log.info("Getting my customer profile");
        CustomerResponse customer = customerService.getCurrentCustomerProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", customer));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateMyProfile(
            @Valid @RequestBody CustomerUpdateRequest request) {
        log.info("Updating my customer profile");
        CustomerResponse customer = customerService.updateCurrentCustomerProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", customer));
    }
}