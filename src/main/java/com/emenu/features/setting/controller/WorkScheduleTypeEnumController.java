
package com.emenu.features.setting.controller;

import com.emenu.features.setting.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.setting.dto.request.WorkScheduleTypeEnumCreateRequest;
import com.emenu.features.setting.dto.response.WorkScheduleTypeEnumResponse;
import com.emenu.features.setting.dto.update.WorkScheduleTypeEnumUpdateRequest;
import com.emenu.features.setting.service.WorkScheduleTypeEnumService;
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
@RequestMapping("/api/v1/enums/work-schedule-type")
@RequiredArgsConstructor
@Slf4j
public class WorkScheduleTypeEnumController {

    private final WorkScheduleTypeEnumService service;

    /**
     * Creates a new work schedule type enum
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WorkScheduleTypeEnumResponse>> create(
            @Valid @RequestBody WorkScheduleTypeEnumCreateRequest request) {
        log.info("Creating work schedule type enum: {}", request.getEnumName());
        WorkScheduleTypeEnumResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Work schedule type enum created", response));
    }

    /**
     * Retrieves a work schedule type enum by its ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkScheduleTypeEnumResponse>> getById(@PathVariable UUID id) {
        log.info("Get work schedule type enum: {}", id);
        WorkScheduleTypeEnumResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Work schedule type enum retrieved", response));
    }

    /**
     * Retrieves all work schedule type enums with pagination and filtering
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<WorkScheduleTypeEnumResponse>>> getAll(
            @Valid @RequestBody ConfigEnumFilterRequest filter) {
        log.info("Get all work schedule type enums");
        PaginationResponse<WorkScheduleTypeEnumResponse> response = service.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Work schedule type enums retrieved", response));
    }

    /**
     * Retrieves all work schedule type enums as a simple list
     */
    @PostMapping("/all-list")
    public ResponseEntity<ApiResponse<List<WorkScheduleTypeEnumResponse>>> getAllList(
            @Valid @RequestBody ConfigEnumFilterRequest filter) {
        log.info("Get all list work schedule type enums");
        List<WorkScheduleTypeEnumResponse> response = service.getAllList(filter);
        return ResponseEntity.ok(ApiResponse.success("Work schedule type enums retrieved", response));
    }

    /**
     * Retrieves work schedule type enums for a specific business
     */
    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<WorkScheduleTypeEnumResponse>>> getByBusinessId(
            @PathVariable UUID businessId) {
        log.info("Get work schedule type enums for business: {}", businessId);
        List<WorkScheduleTypeEnumResponse> responses = service.getByBusinessId(businessId);
        return ResponseEntity.ok(ApiResponse.success("Work schedule type enums retrieved", responses));
    }

    /**
     * Updates a work schedule type enum
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkScheduleTypeEnumResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody WorkScheduleTypeEnumUpdateRequest request) {
        log.info("Update work schedule type enum: {}", id);
        WorkScheduleTypeEnumResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Work schedule type enum updated", response));
    }

    /**
     * Deletes a work schedule type enum
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkScheduleTypeEnumResponse>> delete(@PathVariable UUID id) {
        log.info("Delete work schedule type enum: {}", id);
        WorkScheduleTypeEnumResponse response = service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Work schedule type enum deleted", response));
    }
}