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

    // ========== User Endpoints ==========

    @GetMapping
    public ResponseEntity<List<UserSessionResponse>> getAllSessions() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.getAllSessions(userId));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, String>> logoutSession(@PathVariable UUID sessionId) {
        UUID userId = securityUtils.getCurrentUserId();
        sessionService.logoutSession(sessionId, userId);
        return ResponseEntity.ok(Map.of("message", "Session logged out successfully"));
    }

    @PostMapping("/logout-others")
    public ResponseEntity<Map<String, String>> logoutOtherSessions(@RequestParam UUID currentSessionId) {
        UUID userId = securityUtils.getCurrentUserId();
        sessionService.logoutOtherSessions(userId, currentSessionId);
        return ResponseEntity.ok(Map.of("message", "Other sessions logged out successfully"));
    }

    // ========== Admin Endpoints ==========

    @PostMapping("/admin/all")
    public ResponseEntity<PaginationResponse<AdminSessionResponse>> getAllSessionsAdmin(
            @RequestBody SessionFilterRequest request) {
        return ResponseEntity.ok(sessionService.getAllSessionsAdmin(request));
    }

    @DeleteMapping("/admin/{sessionId}")
    public ResponseEntity<Map<String, String>> logoutSessionAdmin(@PathVariable UUID sessionId) {
        sessionService.logoutSessionAdmin(sessionId);
        return ResponseEntity.ok(Map.of("message", "Session logged out successfully"));
    }

    @PostMapping("/admin/logout-all/{userId}")
    public ResponseEntity<Map<String, String>> logoutAllSessionsAdmin(@PathVariable UUID userId) {
        sessionService.logoutAllSessionsAdmin(userId);
        return ResponseEntity.ok(Map.of("message", "All sessions logged out successfully"));
    }
}
