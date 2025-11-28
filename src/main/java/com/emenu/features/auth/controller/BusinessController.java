package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.service.BusinessService;
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
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
@Slf4j
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<BusinessResponse>>> getAllBusinesses(
            @Valid @RequestBody BusinessFilterRequest request) {
        log.info("Get all businesses");
        PaginationResponse<BusinessResponse> response = businessService.getAllBusinesses(request);
        return ResponseEntity.ok(ApiResponse.success("Businesses retrieved", response));
    }

    @GetMapping("/{businessId}")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessById(@PathVariable UUID businessId) {
        log.info("Get business: {}", businessId);
        BusinessResponse response = businessService.getBusinessById(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business retrieved", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BusinessResponse>> createBusiness(
            @Valid @RequestBody BusinessCreateRequest request) {
        log.info("Create business: {}", request.getName());
        BusinessResponse response = businessService.createBusiness(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business created", response));
    }

    @PutMapping("/{businessId}")
    public ResponseEntity<ApiResponse<BusinessResponse>> updateBusiness(
            @PathVariable UUID businessId,
            @Valid @RequestBody BusinessCreateRequest request) {
        log.info("Update business: {}", businessId);
        BusinessResponse response = businessService.updateBusiness(businessId, request);
        return ResponseEntity.ok(ApiResponse.success("Business updated", response));
    }

    @DeleteMapping("/{businessId}")
    public ResponseEntity<ApiResponse<Void>> deleteBusiness(@PathVariable UUID businessId) {
        log.info("Delete business: {}", businessId);
        businessService.deleteBusiness(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business deleted", null));
    }
}