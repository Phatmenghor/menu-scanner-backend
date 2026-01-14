package com.emenu.features.attendance.controller;

import com.emenu.features.attendance.dto.filter.AttendanceFilterRequest;
import com.emenu.features.attendance.dto.request.AttendanceCheckInRequest;
import com.emenu.features.attendance.dto.request.AttendanceCheckOutRequest;
import com.emenu.features.attendance.dto.response.AttendanceResponse;
import com.emenu.features.attendance.service.AttendanceService;
import com.emenu.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SecurityUtils securityUtils;

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponse> checkIn(@Valid @RequestBody AttendanceCheckInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.checkIn(request));
    }

    @PostMapping("/{attendanceId}/check-out")
    public ResponseEntity<AttendanceResponse> checkOut(
            @PathVariable Long attendanceId,
            @Valid @RequestBody AttendanceCheckOutRequest request) {
        return ResponseEntity.ok(attendanceService.checkOut(attendanceId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponse> getAttendanceById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendanceById(id));
    }

    @GetMapping("/today")
    public ResponseEntity<AttendanceResponse> getTodayAttendance() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(attendanceService.getTodayAttendance(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByUserId(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getAttendanceByUserId(userId, startDate, endDate));
    }

    @GetMapping
    public ResponseEntity<Page<AttendanceResponse>> getAllAttendances(
            AttendanceFilterRequest filterRequest,
            Pageable pageable) {
        return ResponseEntity.ok(attendanceService.getAllAttendances(filterRequest, pageable));
    }
}
