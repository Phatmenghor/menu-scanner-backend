
package com.emenu.features.enums.controller;

import com.emenu.features.enums.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.enums.dto.request.LeaveStatusEnumCreateRequest;
import com.emenu.features.enums.dto.response.LeaveStatusEnumResponse;
import com.emenu.features.enums.dto.response.WorkScheduleTypeEnumResponse;
import com.emenu.features.enums.dto.update.LeaveStatusEnumUpdateRequest;
import com.emenu.features.enums.models.LeaveStatusEnum;
import com.emenu.features.enums.service.LeaveStatusEnumService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enums/leave-status")
@RequiredArgsConstructor
@Slf4j
public class LeaveStatusEnumController {

    private final LeaveStatusEnumService service;

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveStatusEnumResponse>> create(
            @Valid @RequestBody LeaveStatusEnumCreateRequest request) {
        log.info("Creating leave status enum: {}", request.getEnumName());
        LeaveStatusEnumResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave status enum created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveStatusEnumResponse>> getById(@PathVariable UUID id) {
        log.info("Get leave status enum: {}", id);
        LeaveStatusEnumResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Leave status enum retrieved", response));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<LeaveStatusEnumResponse>>> getAll(
            @Valid @RequestBody ConfigEnumFilterRequest filter) {
        log.info("Get all leave status enums");
        PaginationResponse<LeaveStatusEnumResponse> response = service.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Leave status enums retrieved", response));
    }

    @PostMapping("/all-list")
    public ResponseEntity<ApiResponse<List<LeaveStatusEnumResponse>>> getAllList(
            @Valid @RequestBody ConfigEnumFilterRequest filter) {
        log.info("Get all all leave status enums");
        List<LeaveStatusEnumResponse> response = service.getAllList(filter);
        return ResponseEntity.ok(ApiResponse.success("Leave status enums retrieved", response));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<LeaveStatusEnumResponse>>> getByBusinessId(
            @PathVariable UUID businessId) {
        log.info("Get leave status enums for business: {}", businessId);
        List<LeaveStatusEnumResponse> responses = service.getByBusinessId(businessId);
        return ResponseEntity.ok(ApiResponse.success("Leave status enums retrieved", responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveStatusEnumResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LeaveStatusEnumUpdateRequest request) {
        log.info("Update leave status enum: {}", id);
        LeaveStatusEnumResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Leave status enum updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveStatusEnumResponse>> delete(@PathVariable UUID id) {
        log.info("Delete leave status enum: {}", id);
        LeaveStatusEnumResponse response = service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Leave status enum deleted", response));
    }
}