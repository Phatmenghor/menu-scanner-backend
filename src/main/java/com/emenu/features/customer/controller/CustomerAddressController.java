package com.emenu.features.customer.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.customer.dto.filter.CustomerAddressFilterRequest;
import com.emenu.features.customer.dto.request.CustomerAddressCreateRequest;
import com.emenu.features.customer.dto.response.CustomerAddressResponse;
import com.emenu.features.customer.dto.update.CustomerAddressUpdateRequest;
import com.emenu.features.customer.service.CustomerAddressService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Slf4j
public class CustomerAddressController {

    private final CustomerAddressService addressService;
    private final SecurityUtils securityUtils;

    /**
     * Create new address
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> createAddress(@Valid @RequestBody CustomerAddressCreateRequest request) {
        CustomerAddressResponse address = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address created successfully", address));
    }

    /**
     * Get all addresses with filtering and pagination
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<CustomerAddressResponse>>> getAllAddresses(@Valid @RequestBody CustomerAddressFilterRequest filter) {
        log.info("Getting all addresses for current user");
        PaginationResponse<CustomerAddressResponse> addresses = addressService.getAllAddresses(filter);
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved successfully", addresses));
    }

    /**
     * Get my addresses with filtering and pagination
     */
    @PostMapping("/my-addresses/all")
    public ResponseEntity<ApiResponse<PaginationResponse<CustomerAddressResponse>>> getMyAddresses(@Valid @RequestBody CustomerAddressFilterRequest filter) {
        log.info("Getting my addresses for current user");
        User currentUser = securityUtils.getCurrentUser();
        filter.setUserId(currentUser.getId());
        PaginationResponse<CustomerAddressResponse> addresses = addressService.getAllAddresses(filter);
        return ResponseEntity.ok(ApiResponse.success("My addresses retrieved successfully", addresses));
    }

    /**
     * Get my addresses as simple list (backward compatibility)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerAddressResponse>>> getMyAddresses() {
        List<CustomerAddressResponse> addresses = addressService.getMyAddressesList();
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved successfully", addresses));
    }

    /**
     * Get address by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> getAddressById(@PathVariable UUID id) {
        CustomerAddressResponse address = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiResponse.success("Address retrieved successfully", address));
    }

    /**
     * Update address
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> updateAddress(
            @PathVariable UUID id, @Valid @RequestBody CustomerAddressUpdateRequest request) {
        CustomerAddressResponse address = addressService.updateAddress(id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }

    /**
     * Delete address
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> deleteAddress(@PathVariable UUID id) {
        CustomerAddressResponse address = addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", address));
    }

    /**
     * Set default address
     */
    @PutMapping("/{id}/set-default")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> setDefaultAddress(@PathVariable UUID id) {
        CustomerAddressResponse address = addressService.setDefaultAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Default address set successfully", address));
    }

    /**
     * Get default address
     */
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> getDefaultAddress() {
        CustomerAddressResponse address = addressService.getDefaultAddress();
        return ResponseEntity.ok(ApiResponse.success("Default address retrieved successfully", address));
    }
}