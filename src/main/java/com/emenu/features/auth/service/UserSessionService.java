package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.session.UserSessionResponse;
import com.emenu.features.auth.models.RefreshToken;
import com.emenu.features.auth.models.User;
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

    /**
     * Mark expired sessions
     */
    void expireOldSessions();

    /**
     * Clean up old logged out sessions
     */
    void cleanupOldSessions(int daysOld);
}
