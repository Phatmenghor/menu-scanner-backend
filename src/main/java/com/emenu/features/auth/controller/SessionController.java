package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.SessionFilterRequest;
import com.emenu.features.auth.dto.response.AdminSessionResponse;
import com.emenu.features.auth.dto.response.UserSessionResponse;
import com.emenu.features.auth.service.UserSessionService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<List<UserSessionResponse>>> getAllSessions() {
        log.info("Getting all sessions for current user");
        UUID userId = securityUtils.getCurrentUserId();
        List<UserSessionResponse> allSessions = sessionService.getAllSessions(userId);
        return ResponseEntity.ok(ApiResponse.success("All sessions retrieved successfully", allSessions));
    }

    /**
     * Get a session by ID
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<AdminSessionResponse>> getRoleById(
            @PathVariable UUID sessionId) {
        log.info("Get session by ID: {}", sessionId);
        AdminSessionResponse response = sessionService.getSessionById(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session retrieved successfully", response));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<UserSessionResponse>> logoutSession(@PathVariable UUID sessionId) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Session retrieved successfully", sessionService.logoutSession(sessionId, userId)));
    }

    @PostMapping("/logout-others")
    public ResponseEntity<ApiResponse<List<UserSessionResponse>>> logoutOtherSessions(@RequestParam UUID currentSessionId) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Session retrieved successfully", sessionService.logoutOtherSessions(userId, currentSessionId)));
    }

    // ========== Admin Endpoints ==========

    @PostMapping("/admin/all")
    public ResponseEntity<ApiResponse<PaginationResponse<AdminSessionResponse>>> getAllSessionsAdmin(
            @RequestBody SessionFilterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Session retrieved successfully", sessionService.getAllSessionsAdmin(request)));
    }

    @DeleteMapping("/admin/{sessionId}")
    public ResponseEntity<ApiResponse<AdminSessionResponse>> logoutSessionAdmin(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(ApiResponse.success("Session retrieved successfully", sessionService.logoutSessionAdmin(sessionId)));
    }

    @PostMapping("/admin/logout-all/{userId}")
    public ResponseEntity<ApiResponse<List<AdminSessionResponse>>> logoutAllSessionsAdmin(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Session retrieved successfully", sessionService.logoutAllSessionsAdmin(userId)));
    }
}
