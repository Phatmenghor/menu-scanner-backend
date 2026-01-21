package com.emenu.features.hr.controller;

import com.emenu.features.auth.models.User;
import com.emenu.features.hr.dto.filter.LeaveFilterRequest;
import com.emenu.features.hr.dto.request.LeaveApprovalRequest;
import com.emenu.features.hr.dto.request.LeaveCreateRequest;
import com.emenu.features.hr.dto.response.LeaveResponse;
import com.emenu.features.hr.dto.update.LeaveUpdateRequest;
import com.emenu.features.hr.service.LeaveService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/leave")
@RequiredArgsConstructor
@Slf4j
public class LeaveController {

    private final LeaveService service;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveResponse>> create(@Valid @RequestBody LeaveCreateRequest request) {
        log.info("Creating leave request");
        User currentUser = securityUtils.getCurrentUser();
        LeaveResponse response = service.create(request, currentUser.getId(), currentUser.getBusinessId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave request created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveResponse>> getById(@PathVariable UUID id) {
        log.info("Get leave request: {}", id);
        LeaveResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Leave request retrieved", response));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<LeaveResponse>>> getAll(
            @Valid @RequestBody LeaveFilterRequest filter) {
        log.info("Get all leave requests");
        PaginationResponse<LeaveResponse> response = service.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LeaveUpdateRequest request) {
        log.info("Update leave request: {}", id);
        LeaveResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Leave request updated", response));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LeaveResponse>> approve(
            @PathVariable UUID id,
            @Valid @RequestBody LeaveApprovalRequest request) {
        log.info("Processing leave request: {} with status: {}", id, request.getStatus());
        UUID actionBy = securityUtils.getCurrentUserId();
        LeaveResponse response = service.approve(id, request, actionBy);
        return ResponseEntity.ok(ApiResponse.success("Leave request processed", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveResponse>> delete(@PathVariable UUID id) {
        log.info("Delete leave request: {}", id);
        LeaveResponse response = service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Leave request deleted", response));
    }
}