package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.SessionFilterRequest;
import com.emenu.features.auth.dto.session.AdminSessionResponse;
import com.emenu.features.auth.dto.session.UserSessionResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface UserSessionService {
    /**
     * Get all sessions (active and inactive) for a user
     */
    List<UserSessionResponse> getAllSessions(UUID userId);

    /**
     * Get session by ID for a specific user
     */
    UserSessionResponse getSessionById(UUID sessionId, UUID userId);

    /**
     * Get session by ID (admin access)
     */
    AdminSessionResponse getSessionByIdAdmin(UUID sessionId);

    /**
     * Get all sessions with filters and pagination (admin view)
     */
    PaginationResponse<AdminSessionResponse> getAllSessionsAdmin(SessionFilterRequest request);

    /**
     * Logout from a specific session/device
     */
    void logoutSession(UUID sessionId, UUID userId);

    /**
     * Logout from all devices except current
     */
    void logoutAllOtherDevices(UUID userId, UUID currentSessionId);

    /**
     * Logout from all devices
     */
    void logoutAllDevices(UUID userId);

}
