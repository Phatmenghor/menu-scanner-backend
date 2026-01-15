package com.emenu.features.hr.controller;

import com.emenu.features.hr.dto.filter.AttendancePolicyFilterRequest;
import com.emenu.features.hr.dto.request.AttendancePolicyCreateRequest;
import com.emenu.features.hr.dto.response.AttendancePolicyResponse;
import com.emenu.features.hr.dto.update.AttendancePolicyUpdateRequest;
import com.emenu.features.hr.service.AttendancePolicyService;
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
@RequestMapping("/api/v1/hr/attendance-policy")
@RequiredArgsConstructor
@Slf4j
public class AttendancePolicyController {

    private final AttendancePolicyService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<AttendancePolicyResponse>> create(
            @Valid @RequestBody AttendancePolicyCreateRequest request) {
        log.info("Creating attendance policy: {}", request.getPolicyName());
        AttendancePolicyResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance policy created", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_STAFF', 'BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<AttendancePolicyResponse>> getById(@PathVariable UUID id) {
        log.info("Get attendance policy: {}", id);
        AttendancePolicyResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance policy retrieved", response));
    }

    @PostMapping("/all")
    @PreAuthorize("hasAnyRole('BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<PaginationResponse<AttendancePolicyResponse>>> getAll(
            @Valid @RequestBody AttendancePolicyFilterRequest filter) {
        log.info("Get all attendance policies");
        PaginationResponse<AttendancePolicyResponse> response = service.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Attendance policies retrieved", response));
    }

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasAnyRole('BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<List<AttendancePolicyResponse>>> getByBusinessId(
            @PathVariable UUID businessId) {
        log.info("Get attendance policies for business: {}", businessId);
        List<AttendancePolicyResponse> responses = service.getByBusinessId(businessId);
        return ResponseEntity.ok(ApiResponse.success("Attendance policies retrieved", responses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<AttendancePolicyResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AttendancePolicyUpdateRequest request) {
        log.info("Update attendance policy: {}", id);
        AttendancePolicyResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance policy updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUSINESS_MANAGER', 'BUSINESS_OWNER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        log.info("Delete attendance policy: {}", id);
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance policy deleted", null));
    }
}