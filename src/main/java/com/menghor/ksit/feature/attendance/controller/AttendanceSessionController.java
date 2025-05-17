package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.feature.attendance.dto.request.AttendanceSessionRequest;
import com.menghor.ksit.feature.attendance.dto.request.QrAttendanceRequest;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceSessionDto;
import com.menghor.ksit.feature.attendance.dto.response.QrResponse;
import com.menghor.ksit.feature.attendance.service.AttendanceSessionService;
import com.menghor.ksit.feature.auth.models.UserEntity;
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
@RequestMapping("/api/v1/attendance-sessions")
@RequiredArgsConstructor
public class AttendanceSessionController {
    private final AttendanceSessionService sessionService;
    private final SecurityUtils securityUtils;
    
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceSessionDto> getAttendanceSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.findById(id));
    }
    
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<AttendanceSessionDto>> getSessionsBySchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(sessionService.findByScheduleId(scheduleId));
    }
    
    @PostMapping("/search")
    public ResponseEntity<Page<AttendanceSessionDto>> searchAttendanceSessions(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Boolean isFinal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sessionDate").descending());
        return ResponseEntity.ok(sessionService.findAll(teacherId, scheduleId, classId, courseId, isFinal, startDate, endDate, pageable));
    }
    
    @PostMapping("/generate")
    public ResponseEntity<AttendanceSessionDto> generateSession(@RequestBody AttendanceSessionRequest request) {
        UserEntity currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(sessionService.generateAttendanceSession(request, 4L));
    }
    
    @PostMapping("/qr/{sessionId}")
    public ResponseEntity<QrResponse> generateQrCode(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.generateQrCode(sessionId));
    }
    
    @PostMapping("/mark-by-qr")
    public ResponseEntity<AttendanceSessionDto> markAttendanceByQr(@RequestBody QrAttendanceRequest request) {
        return ResponseEntity.ok(sessionService.markAttendanceByQr(request));
    }
    
    @PostMapping("/finalize/{sessionId}")
    public ResponseEntity<AttendanceSessionDto> finalizeSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.finalizeAttendanceSession(sessionId));
    }
}