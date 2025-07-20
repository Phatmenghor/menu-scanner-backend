package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.DashboardStatsResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
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
@RequestMapping("/api/v1/business")
@RequiredArgsConstructor
@Slf4j
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping
    public ResponseEntity<ApiResponse<BusinessResponse>> createBusiness(@Valid @RequestBody BusinessCreateRequest request) {
        log.info("Creating business: {}", request.getName());
        BusinessResponse business = businessService.createBusiness(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business created successfully", business));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<BusinessResponse>>> getBusinesses(@ModelAttribute BusinessFilterRequest filter) {
        log.info("Getting businesses with filter");
        // ✅ Service returns pagination response directly from mapper
        PaginationResponse<BusinessResponse> businesses = businessService.getBusinesses(filter);
        return ResponseEntity.ok(ApiResponse.success("Businesses retrieved successfully", businesses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessById(@PathVariable UUID id) {
        log.info("Getting business by ID: {}", id);
        BusinessResponse business = businessService.getBusinessById(id);
        return ResponseEntity.ok(ApiResponse.success("Business retrieved successfully", business));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessResponse>> updateBusiness(
            @PathVariable UUID id,
            @Valid @RequestBody BusinessUpdateRequest request) {
        log.info("Updating business: {}", id);
        BusinessResponse business = businessService.updateBusiness(id, request);
        return ResponseEntity.ok(ApiResponse.success("Business updated successfully", business));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBusiness(@PathVariable UUID id) {
        log.info("Deleting business: {}", id);
        businessService.deleteBusiness(id);
        return ResponseEntity.ok(ApiResponse.success("Business deleted successfully", null));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateBusiness(@PathVariable UUID id) {
        log.info("Activating business: {}", id);
        businessService.activateBusiness(id);
        return ResponseEntity.ok(ApiResponse.success("Business activated successfully", null));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendBusiness(@PathVariable UUID id) {
        log.info("Suspending business: {}", id);
        businessService.suspendBusiness(id);
        return ResponseEntity.ok(ApiResponse.success("Business suspended successfully", null));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getBusinessStats(@PathVariable UUID id) {
        log.info("Getting business stats: {}", id);
        DashboardStatsResponse stats = businessService.getBusinessStats(id);
        return ResponseEntity.ok(ApiResponse.success("Business stats retrieved successfully", stats));
    }

    @GetMapping("/{id}/staff")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getBusinessStaff(
            @PathVariable UUID id,
            @ModelAttribute UserFilterRequest filter) {
        log.info("Getting business staff: {}", id);
        // ✅ Service returns pagination response directly from mapper
        PaginationResponse<UserResponse> staff = businessService.getBusinessStaff(id, filter);
        return ResponseEntity.ok(ApiResponse.success("Business staff retrieved successfully", staff));
    }

    @GetMapping("/{id}/can-add-staff")
    public ResponseEntity<ApiResponse<Boolean>> canAddMoreStaff(@PathVariable UUID id) {
        log.info("Checking if business can add more staff: {}", id);
        boolean canAdd = businessService.canAddMoreStaff(id);
        return ResponseEntity.ok(ApiResponse.success("Staff limit check completed", canAdd));
    }
}