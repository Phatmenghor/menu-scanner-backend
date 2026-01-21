
package com.emenu.features.setting.controller;

import com.emenu.features.setting.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.setting.dto.request.LeaveTypeEnumCreateRequest;
import com.emenu.features.setting.dto.response.LeaveTypeEnumResponse;
import com.emenu.features.setting.dto.update.LeaveTypeEnumUpdateRequest;
import com.emenu.features.setting.service.LeaveTypeEnumService;
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
@RequestMapping("/api/v1/enums/leave-type")
@RequiredArgsConstructor
@Slf4j
public class LeaveTypeEnumController {

    private final LeaveTypeEnumService service;

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveTypeEnumResponse>> create(
            @Valid @RequestBody LeaveTypeEnumCreateRequest request) {
        log.info("Creating leave type enum: {}", request.getEnumName());
        LeaveTypeEnumResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave type enum created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveTypeEnumResponse>> getById(@PathVariable UUID id) {
        log.info("Get leave type enum: {}", id);
        LeaveTypeEnumResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Leave type enum retrieved", response));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<LeaveTypeEnumResponse>>> getAll(
            @Valid @RequestBody ConfigEnumFilterRequest filter) {
        log.info("Get all leave type enums");
        PaginationResponse<LeaveTypeEnumResponse> response = service.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Leave type enums retrieved", response));
    }

    @PostMapping("/all-list")
    public ResponseEntity<ApiResponse<List<LeaveTypeEnumResponse>>> getAllList(
            @Valid @RequestBody ConfigEnumFilterRequest filter) {
        log.info("Get all list leave type enums");
        List<LeaveTypeEnumResponse> response = service.getAllList(filter);
        return ResponseEntity.ok(ApiResponse.success("Leave type enums retrieved", response));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<LeaveTypeEnumResponse>>> getByBusinessId(
            @PathVariable UUID businessId) {
        log.info("Get leave type enums for business: {}", businessId);
        List<LeaveTypeEnumResponse> responses = service.getByBusinessId(businessId);
        return ResponseEntity.ok(ApiResponse.success("Leave type enums retrieved", responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveTypeEnumResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LeaveTypeEnumUpdateRequest request) {
        log.info("Update leave type enum: {}", id);
        LeaveTypeEnumResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Leave type enum updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveTypeEnumResponse>> delete(@PathVariable UUID id) {
        log.info("Delete leave type enum: {}", id);
        LeaveTypeEnumResponse response = service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Leave type enum deleted", response));
    }
}