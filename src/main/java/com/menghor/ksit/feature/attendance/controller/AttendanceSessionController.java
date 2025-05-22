package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.attendance.dto.request.AttendanceSessionRequest;
import com.menghor.ksit.feature.attendance.dto.request.QrAttendanceRequest;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceSessionDto;
import com.menghor.ksit.feature.attendance.service.AttendanceSessionService;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance-sessions")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AttendanceSessionController {

    private final AttendanceSessionService sessionService;
    private final SecurityUtils securityUtils;

    @GetMapping("/{id}")
    public ApiResponse<AttendanceSessionDto> getAttendanceSession(
            @PathVariable @NotNull @Positive Long id) {

        log.info("Getting attendance session with ID: {}", id);

        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("User {} retrieving attendance session {}", currentUser.getUsername(), id);

        AttendanceSessionDto session = sessionService.findById(id);

        log.info("Successfully retrieved attendance session - ID: {}",
                session.getId());

        return new ApiResponse<>(
                "success",
                "Attendance session retrieved successfully",
                session
        );
    }

    @PostMapping("/generate")
    public ApiResponse<AttendanceSessionDto> generateSession(
            @Valid @RequestBody AttendanceSessionRequest request) {

        log.info("Generating new attendance session for subject");

        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("User {} generating attendance session", currentUser.getUsername());

        AttendanceSessionDto session = sessionService.generateAttendanceSession(request, 4L);

        log.info("Successfully generated attendance session - ID: {}",
                session.getId());

        return new ApiResponse<>(
                "success",
                "Attendance session generated successfully",
                session
        );
    }

    @PostMapping("/mark-by-qr")
    public ApiResponse<AttendanceSessionDto> markAttendanceByQr(
            @Valid @RequestBody QrAttendanceRequest request) {

        log.info("Processing QR attendance for session ID");

        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("User {} marking attendance for session",
                currentUser.getUsername());

        AttendanceSessionDto session = sessionService.markAttendanceByQr(request);

        log.info("Successfully marked attendance - User: {}, Session: {}",
                currentUser.getUsername(), session.getId());

        return new ApiResponse<>(
                "success",
                "Attendance marked successfully",
                session
        );
    }

    @PostMapping("/finalize/{sessionId}")
    public ApiResponse<AttendanceSessionDto> finalizeSession(
            @PathVariable @NotNull @Positive Long sessionId) {

        log.info("Finalizing attendance session with ID: {}", sessionId);

        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("User {} finalizing session {}", currentUser.getUsername(), sessionId);

        AttendanceSessionDto session = sessionService.finalizeAttendanceSession(sessionId);

        log.info("Successfully finalized attendance session - ID: {}",
                session.getId());

        return new ApiResponse<>(
                "success",
                "Attendance session finalized successfully",
                session
        );
    }
}