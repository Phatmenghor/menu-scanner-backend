package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.SessionFilterRequest;
import com.emenu.features.auth.dto.session.AdminSessionResponse;
import com.emenu.features.auth.dto.session.UserSessionResponse;
import com.emenu.features.auth.models.RefreshToken;
import com.emenu.features.auth.models.User;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface UserSessionService {

    /**
     * Create a new user session
     */
    void createSession(User user, RefreshToken refreshToken, HttpServletRequest request);

    /**
     * Update last active time for current session
     */
    void updateLastActive(UUID userId, String deviceId);

    /**
     * Get all active sessions for a user
     */
    List<UserSessionResponse> getActiveSessions(UUID userId);

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
     * Logout from a specific session (admin access - no user verification)
     */
    void logoutSessionAdmin(UUID sessionId);

    /**
     * Logout from all devices except current
     */
    void logoutAllOtherDevices(UUID userId, UUID currentSessionId);

    /**
     * Logout from all devices
     */
    void logoutAllDevices(UUID userId);

    /**
     * Mark expired sessions
     */
    void expireOldSessions();

    /**
     * Clean up old logged out sessions
     */
    void cleanupOldSessions(int daysOld);
}
