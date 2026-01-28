package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.SessionFilterRequest;
import com.emenu.features.auth.dto.session.AdminSessionResponse;
import com.emenu.features.auth.dto.session.UserSessionResponse;
import com.emenu.features.auth.service.UserSessionService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final UserSessionService sessionService;
    private final SecurityUtils securityUtils;

    // ========== User Endpoints ==========

    @GetMapping
    public ResponseEntity<List<UserSessionResponse>> getAllSessions() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.getAllSessions(userId));
    }

    /**
     * Get a session by ID
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<AdminSessionResponse>> getRoleById(
            @PathVariable UUID sessionId) {
        log.info("Get role by ID: {}", sessionId);
        AdminSessionResponse response = sessionService.getSessionById(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session retrieved", response));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<UserSessionResponse> logoutSession(@PathVariable UUID sessionId) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.logoutSession(sessionId, userId));
    }

    @PostMapping("/logout-others")
    public ResponseEntity<List<UserSessionResponse>> logoutOtherSessions(@RequestParam UUID currentSessionId) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.logoutOtherSessions(userId, currentSessionId));
    }

    // ========== Admin Endpoints ==========

    @PostMapping("/admin/all")
    public ResponseEntity<PaginationResponse<AdminSessionResponse>> getAllSessionsAdmin(
            @RequestBody SessionFilterRequest request) {
        return ResponseEntity.ok(sessionService.getAllSessionsAdmin(request));
    }

    @DeleteMapping("/admin/{sessionId}")
    public ResponseEntity<AdminSessionResponse> logoutSessionAdmin(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(sessionService.logoutSessionAdmin(sessionId));
    }

    @PostMapping("/admin/logout-all/{userId}")
    public ResponseEntity<List<AdminSessionResponse>> logoutAllSessionsAdmin(@PathVariable UUID userId) {
        return ResponseEntity.ok(sessionService.logoutAllSessionsAdmin(userId));
    }
}
