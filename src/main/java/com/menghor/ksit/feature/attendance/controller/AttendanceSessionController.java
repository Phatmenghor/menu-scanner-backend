package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.feature.attendance.dto.request.AttendanceSessionRequest;
import com.menghor.ksit.feature.attendance.dto.request.QrAttendanceRequest;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceSessionDto;
import com.menghor.ksit.feature.attendance.dto.response.QrResponse;
import com.menghor.ksit.feature.attendance.service.AttendanceSessionService;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.QR.QrCodeGenerator;
import com.menghor.ksit.utils.database.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/v1/attendance-sessions")
@RequiredArgsConstructor
@Slf4j
public class AttendanceSessionController {
    private final AttendanceSessionService sessionService;
    private final SecurityUtils securityUtils;


    @GetMapping("/{id}")
    public ResponseEntity<AttendanceSessionDto> getAttendanceSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.findById(id));
    }

    @PostMapping("/generate")
    public ResponseEntity<AttendanceSessionDto> generateSession(@RequestBody AttendanceSessionRequest request) {
        UserEntity currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(sessionService.generateAttendanceSession(request, 4L));
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