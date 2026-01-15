package com.emenu.features.hr.controller;

import com.emenu.features.hr.dto.filter.WorkScheduleFilterRequest;
import com.emenu.features.hr.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.hr.dto.response.WorkScheduleResponse;
import com.emenu.features.hr.dto.update.WorkScheduleUpdateRequest;
import com.emenu.features.hr.service.WorkScheduleService;
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
@RequestMapping("/api/v1/hr/work-schedule")
@RequiredArgsConstructor
@Slf4j
public class WorkScheduleController {

    private final WorkScheduleService service;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> create(
            @Valid @RequestBody WorkScheduleCreateRequest request) {
        log.info("Creating work schedule: {}", request.getName());
        WorkScheduleResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Work schedule created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> getById(@PathVariable UUID id) {
        log.info("Get work schedule: {}", id);
        WorkScheduleResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Work schedule retrieved", response));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<WorkScheduleResponse>>> getAll(
            @Valid @RequestBody WorkScheduleFilterRequest filter) {
        log.info("Get all work schedules");
        PaginationResponse<WorkScheduleResponse> response = service.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Work schedules retrieved", response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<WorkScheduleResponse>>> getByUserId(
            @PathVariable UUID userId) {
        log.info("Get work schedules for user: {}", userId);
        List<WorkScheduleResponse> responses = service.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Work schedules retrieved", responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody WorkScheduleUpdateRequest request) {
        log.info("Update work schedule: {}", id);
        WorkScheduleResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Work schedule updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> delete(@PathVariable UUID id) {
        log.info("Delete work schedule: {}", id);
        WorkScheduleResponse response = service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Work schedule deleted", response));
    }
}