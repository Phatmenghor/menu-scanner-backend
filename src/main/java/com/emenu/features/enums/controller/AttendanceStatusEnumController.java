
package com.emenu.features.enums.controller;

import com.emenu.features.enums.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.enums.dto.request.AttendanceStatusEnumCreateRequest;
import com.emenu.features.enums.dto.response.AttendanceStatusEnumResponse;
import com.emenu.features.enums.dto.update.AttendanceStatusEnumUpdateRequest;
import com.emenu.features.enums.service.AttendanceStatusEnumService;
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
@RequestMapping("/api/v1/enums/attendance-status")
@RequiredArgsConstructor
@Slf4j
public class AttendanceStatusEnumController {

    private final AttendanceStatusEnumService service;

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceStatusEnumResponse>> create(
            @Valid @RequestBody AttendanceStatusEnumCreateRequest request) {
        log.info("Creating attendance status enum: {}", request.getEnumName());
        AttendanceStatusEnumResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance status enum created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceStatusEnumResponse>> getById(@PathVariable UUID id) {
        log.info("Get attendance status enum: {}", id);
        AttendanceStatusEnumResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance status enum retrieved", response));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<AttendanceStatusEnumResponse>>> getAll(
            @Valid @RequestBody ConfigEnumFilterRequest filter) {
        log.info("Get all attendance status enums");
        PaginationResponse<AttendanceStatusEnumResponse> response = service.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Attendance status enums retrieved", response));
    }

    @PostMapping("/all-list")
    public ResponseEntity<ApiResponse<List<AttendanceStatusEnumResponse>>> getAllList(
            @Valid @RequestBody ConfigEnumFilterRequest filter) {
        log.info("Get all list attendance status enums");
        List<AttendanceStatusEnumResponse> response = service.getAllList(filter);
        return ResponseEntity.ok(ApiResponse.success("Attendance status enums retrieved", response));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<List<AttendanceStatusEnumResponse>>> getByBusinessId(
            @PathVariable UUID businessId) {
        log.info("Get attendance status enums for business: {}", businessId);
        List<AttendanceStatusEnumResponse> responses = service.getByBusinessId(businessId);
        return ResponseEntity.ok(ApiResponse.success("Attendance status enums retrieved", responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceStatusEnumResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AttendanceStatusEnumUpdateRequest request) {
        log.info("Update attendance status enum: {}", id);
        AttendanceStatusEnumResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance status enum updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceStatusEnumResponse>> delete(@PathVariable UUID id) {
        log.info("Delete attendance status enum: {}", id);
        AttendanceStatusEnumResponse response = service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance status enum deleted", response));
    }
}