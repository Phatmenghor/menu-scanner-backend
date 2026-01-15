package com.emenu.features.hr.controller;

import com.emenu.features.hr.dto.filter.LeavePolicyFilterRequest;
import com.emenu.features.hr.dto.request.LeavePolicyCreateRequest;
import com.emenu.features.hr.dto.response.LeavePolicyResponse;
import com.emenu.features.hr.dto.update.LeavePolicyUpdateRequest;
import com.emenu.features.hr.service.LeaveService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/leave-policy")
@RequiredArgsConstructor
@Slf4j
public class LeavePolicyController {

    // Note: Will need to create LeavePolicyService if not exists
    // For now using LeaveService as placeholder
    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> create(
            @Valid @RequestBody LeavePolicyCreateRequest request) {
        log.info("Creating leave policy: {}", request.getPolicyName());
        // Implementation will use dedicated LeavePolicyService
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave policy created", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_STAFF', 'BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> getById(@PathVariable UUID id) {
        log.info("Get leave policy: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Leave policy retrieved", null));
    }

    @PostMapping("/all")
    @PreAuthorize("hasAnyRole('BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<PaginationResponse<LeavePolicyResponse>>> getAll(
            @Valid @RequestBody LeavePolicyFilterRequest filter) {
        log.info("Get all leave policies");
        return ResponseEntity.ok(ApiResponse.success("Leave policies retrieved", null));
    }

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasAnyRole('BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<List<LeavePolicyResponse>>> getByBusinessId(
            @PathVariable UUID businessId) {
        log.info("Get leave policies for business: {}", businessId);
        return ResponseEntity.ok(ApiResponse.success("Leave policies retrieved", null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LeavePolicyUpdateRequest request) {
        log.info("Update leave policy: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Leave policy updated", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        log.info("Delete leave policy: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Leave policy deleted", null));
    }
}