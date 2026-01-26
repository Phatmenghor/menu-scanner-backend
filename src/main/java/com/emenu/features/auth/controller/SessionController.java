package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.SessionFilterRequest;
import com.emenu.features.auth.dto.session.AdminSessionResponse;
import com.emenu.features.auth.dto.session.UserSessionResponse;
import com.emenu.features.auth.service.UserSessionService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final UserSessionService sessionService;
    private final SecurityUtils securityUtils;

    @GetMapping("/active")
    public ResponseEntity<List<UserSessionResponse>> getActiveSessions() {
        UUID userId = securityUtils.getCurrentUserId();
        log.info("User {} requesting active sessions", userId);
        return ResponseEntity.ok(sessionService.getActiveSessions(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserSessionResponse>> getAllSessions() {
        UUID userId = securityUtils.getCurrentUserId();
        log.info("User {} requesting all sessions", userId);
        return ResponseEntity.ok(sessionService.getAllSessions(userId));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<UserSessionResponse> getSessionById(@PathVariable UUID sessionId) {
        UUID userId = securityUtils.getCurrentUserId();
        log.info("User {} requesting session detail: {}", userId, sessionId);
        return ResponseEntity.ok(sessionService.getSessionById(sessionId, userId));
    }

    // ========== Admin Endpoints ==========

    @PostMapping("/admin/all")
    public ResponseEntity<PaginationResponse<AdminSessionResponse>> getAllSessionsAdmin(
            @RequestBody SessionFilterRequest request) {
        log.info("Admin requesting all sessions with filters");
        return ResponseEntity.ok(sessionService.getAllSessionsAdmin(request));
    }

    @GetMapping("/admin/{sessionId}")
    public ResponseEntity<AdminSessionResponse> getSessionByIdAdmin(@PathVariable UUID sessionId) {
        log.info("Admin requesting session detail: {}", sessionId);
        return ResponseEntity.ok(sessionService.getSessionByIdAdmin(sessionId));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, String>> logoutSession(@PathVariable UUID sessionId) {
        UUID userId = securityUtils.getCurrentUserId();
        log.info("User {} logging out from session {}", userId, sessionId);
        sessionService.logoutSession(sessionId, userId);
        return ResponseEntity.ok(Map.of("message", "Successfully logged out from device"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Map<String, String>> logoutAllDevices() {
        UUID userId = securityUtils.getCurrentUserId();
        log.warn("User {} logging out from ALL devices", userId);
        sessionService.logoutAllDevices(userId);
        return ResponseEntity.ok(Map.of("message", "Successfully logged out from all devices"));
    }

    @PostMapping("/logout-others")
    public ResponseEntity<Map<String, String>> logoutOtherDevices(@RequestParam UUID currentSessionId) {
        UUID userId = securityUtils.getCurrentUserId();
        log.info("User {} logging out from other devices, keeping session {}", userId, currentSessionId);
        sessionService.logoutAllOtherDevices(userId, currentSessionId);
        return ResponseEntity.ok(Map.of("message", "Successfully logged out from other devices"));
    }
}
