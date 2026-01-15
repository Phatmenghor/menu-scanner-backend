package com.emenu.features.hr.controller;

import com.emenu.features.hr.dto.filter.LeavePolicyFilterRequest;
import com.emenu.features.hr.dto.request.LeavePolicyCreateRequest;
import com.emenu.features.hr.dto.response.LeavePolicyResponse;
import com.emenu.features.hr.dto.update.LeavePolicyUpdateRequest;
import com.emenu.features.hr.service.LeavePolicyService;
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
@RequestMapping("/api/v1/hr/leave-policy")
@RequiredArgsConstructor
@Slf4j
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> create(
            @Valid @RequestBody LeavePolicyCreateRequest request) {
        log.info("Creating leave policy: {}", request.getPolicyName());
        LeavePolicyResponse response = leavePolicyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave policy created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> getById(@PathVariable UUID id) {
        log.info("Get leave policy: {}", id);
        LeavePolicyResponse response = leavePolicyService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Leave policy retrieved", response));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<LeavePolicyResponse>>> getAll(
            @Valid @RequestBody LeavePolicyFilterRequest filter) {
        log.info("Get all leave policies");
        PaginationResponse<LeavePolicyResponse> response = leavePolicyService.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Leave policies retrieved", response));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<LeavePolicyResponse>>> getByBusinessId(
            @PathVariable UUID businessId) {
        log.info("Get leave policies for business: {}", businessId);
        List<LeavePolicyResponse> response = leavePolicyService.getByBusinessId(businessId);
        return ResponseEntity.ok(ApiResponse.success("Leave policies retrieved", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LeavePolicyUpdateRequest request) {
        log.info("Update leave policy: {}", id);
        LeavePolicyResponse response = leavePolicyService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Leave policy updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> delete(@PathVariable UUID id) {
        log.info("Delete leave policy: {}", id);
        LeavePolicyResponse response = leavePolicyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Leave policy deleted", response));
    }
}