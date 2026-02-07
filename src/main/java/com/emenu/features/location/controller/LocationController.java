package com.emenu.features.location.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.location.dto.filter.LocationFilterRequest;
import com.emenu.features.location.dto.request.LocationCreateRequest;
import com.emenu.features.location.dto.response.LocationResponse;
import com.emenu.features.location.dto.update.LocationUpdateRequest;
import com.emenu.features.location.service.LocationService;
import com.emenu.security.SecurityUtils;
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
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService addressService;
    private final SecurityUtils securityUtils;

    /**
     * Create new address
     */
    @PostMapping
    public ResponseEntity<ApiResponse<LocationResponse>> createAddress(@Valid @RequestBody LocationCreateRequest request) {
        log.info("Creating new address");
        LocationResponse address = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address created successfully", address));
    }

    /**
     * Get all addresses with filtering and pagination
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<LocationResponse>>> getAllAddresses(@Valid @RequestBody LocationFilterRequest filter) {
        log.info("Getting all addresses for current user");
        PaginationResponse<LocationResponse> addresses = addressService.getAllAddresses(filter);
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved successfully", addresses));
    }

    /**
     * Get my addresses with filtering and pagination
     */
    @PostMapping("/my-addresses/all")
    public ResponseEntity<ApiResponse<PaginationResponse<LocationResponse>>> getMyAddresses(@Valid @RequestBody LocationFilterRequest filter) {
        log.info("Getting my addresses for current user");
        User currentUser = securityUtils.getCurrentUser();
        filter.setUserId(currentUser.getId());
        PaginationResponse<LocationResponse> addresses = addressService.getAllAddresses(filter);
        return ResponseEntity.ok(ApiResponse.success("My addresses retrieved successfully", addresses));
    }

    /**
     * Get address by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationResponse>> getAddressById(@PathVariable UUID id) {
        log.info("Get address by id: {}", id);
        LocationResponse address = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiResponse.success("Address retrieved successfully", address));
    }

    /**
     * Update address
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationResponse>> updateAddress(
            @PathVariable UUID id, @Valid @RequestBody LocationUpdateRequest request) {
        log.info("Update address: {}", id);
        LocationResponse address = addressService.updateAddress(id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }

    /**
     * Delete address
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationResponse>> deleteAddress(@PathVariable UUID id) {
        log.info("Delete address: {}", id);
        LocationResponse address = addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", address));
    }

    /**
     * Get default address
     */
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<LocationResponse>> getDefaultAddress() {
        log.info("Get default address");
        LocationResponse address = addressService.getDefaultAddress();
        return ResponseEntity.ok(ApiResponse.success("Default address retrieved successfully", address));
    }
}