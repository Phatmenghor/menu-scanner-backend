package com.emenu.features.hr.controller;

import com.emenu.features.hr.dto.filter.AttendanceFilterRequest;
import com.emenu.features.hr.dto.request.AttendanceCheckInRequest;
import com.emenu.features.hr.dto.response.AttendanceResponse;
import com.emenu.features.hr.dto.update.AttendanceUpdateRequest;
import com.emenu.features.hr.service.AttendanceService;
import com.emenu.security.SecurityUtils;
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
@RequestMapping("/api/v1/hr/attendance")
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final AttendanceService service;
    private final SecurityUtils securityUtils;

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
            @Valid @RequestBody AttendanceCheckInRequest request) {
        log.info("Processing check-in request");
        UUID userId = securityUtils.getCurrentUserId();
        UUID businessId = securityUtils.getCurrentUserBusinessId();
        AttendanceResponse response = service.checkIn(request, userId, businessId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Check-in recorded successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getById(@PathVariable UUID id) {
        log.info("Get attendance record: {}", id);
        AttendanceResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance record retrieved", response));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<AttendanceResponse>>> getAll(
            @Valid @RequestBody AttendanceFilterRequest filter) {
        log.info("Get all attendance records");
        PaginationResponse<AttendanceResponse> response = service.getAll(filter);
        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AttendanceUpdateRequest request) {
        log.info("Update attendance record: {}", id);
        AttendanceResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance record updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> delete(@PathVariable UUID id) {
        log.info("Delete attendance record: {}", id);
        AttendanceResponse response = service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance record deleted", response));
    }
}