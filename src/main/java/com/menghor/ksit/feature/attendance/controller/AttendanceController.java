package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceDto;
import com.menghor.ksit.feature.attendance.dto.update.AttendanceUpdateRequest;
import com.menghor.ksit.feature.attendance.service.AttendanceService;
import com.menghor.ksit.feature.attendance.service.AttendanceSessionService;
import com.menghor.ksit.utils.database.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final AttendanceSessionService sessionService;
    private final SecurityUtils securityUtils;
    
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceDto> getAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.findById(id));
    }
    
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AttendanceDto>> getAttendanceBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.findBySessionId(sessionId));
    }
    
    @PostMapping("/search")
    public ResponseEntity<Page<AttendanceDto>> searchAttendance(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("recordedTime").descending());
        return ResponseEntity.ok(attendanceService.findAll(studentId, sessionId, status, startDate, endDate, pageable));
    }
    
    @PutMapping("/update")
    public ResponseEntity<AttendanceDto> updateAttendance(@RequestBody AttendanceUpdateRequest request) {
        return ResponseEntity.ok(attendanceService.updateAttendance(request));
    }
}