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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance-sessions")
@RequiredArgsConstructor
public class AttendanceSessionController {
    private final AttendanceSessionService sessionService;
    private final SecurityUtils securityUtils;
    private final QrCodeGenerator qrCodeGenerator;

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceSessionDto> getAttendanceSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.findById(id));
    }
    
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<AttendanceSessionDto>> getSessionsBySchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(sessionService.findByScheduleId(scheduleId));
    }
    
    @PostMapping("/all")
    public ResponseEntity<Page<AttendanceSessionDto>> searchAttendanceSessions(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Boolean isFinal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sessionDate").descending());
        return ResponseEntity.ok(sessionService.findAll(teacherId, scheduleId, classId, isFinal, pageable));
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


    @GetMapping(value = "qr-code/{sessionId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCodeImage(@PathVariable Long sessionId) {
        String qrCode = sessionService.generateQrCode(sessionId).getQrCode();
        String base64QrCode = qrCodeGenerator.generateQrCodeBase64(qrCode, 250, 250);
        byte[] qrCodeImage = Base64.getDecoder().decode(base64QrCode);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCodeImage);
    }
}