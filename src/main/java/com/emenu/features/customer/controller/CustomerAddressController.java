package com.emenu.features.customer.controller;

import com.emenu.features.customer.dto.request.CustomerAddressCreateRequest;
import com.emenu.features.customer.dto.response.CustomerAddressResponse;
import com.emenu.features.customer.dto.update.CustomerAddressUpdateRequest;
import com.emenu.features.customer.service.CustomerAddressService;
import com.emenu.shared.dto.ApiResponse;
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

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> createAddress(@Valid @RequestBody CustomerAddressCreateRequest request) {
        CustomerAddressResponse address = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address created successfully", address));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerAddressResponse>>> getMyAddresses() {
        List<CustomerAddressResponse> addresses = addressService.getMyAddresses();
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved successfully", addresses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> getAddressById(@PathVariable UUID id) {
        CustomerAddressResponse address = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiResponse.success("Address retrieved successfully", address));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> updateAddress(
            @PathVariable UUID id, @Valid @RequestBody CustomerAddressUpdateRequest request) {
        CustomerAddressResponse address = addressService.updateAddress(id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> deleteAddress(@PathVariable UUID id) {
        CustomerAddressResponse address = addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", address));
    }

    @PutMapping("/{id}/set-default")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> setDefaultAddress(@PathVariable UUID id) {
        CustomerAddressResponse address = addressService.setDefaultAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Default address set successfully", address));
    }

    @GetMapping("/default")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> getDefaultAddress() {
        CustomerAddressResponse address = addressService.getDefaultAddress();
        return ResponseEntity.ok(ApiResponse.success("Default address retrieved successfully", address));
    }
}