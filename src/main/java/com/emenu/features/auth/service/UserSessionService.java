package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.SessionFilterRequest;
import com.emenu.features.auth.dto.session.AdminSessionResponse;
import com.emenu.features.auth.dto.session.UserSessionResponse;
import com.emenu.features.auth.models.RefreshToken;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.models.UserSession;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface UserSessionService {

    // ========== Session Creation ==========

    UserSession createSession(User user, RefreshToken refreshToken, HttpServletRequest request);

    // ========== User Endpoints ==========

    List<UserSessionResponse> getAllSessions(UUID userId);

    void logoutSession(UUID sessionId, UUID userId);

    void logoutOtherSessions(UUID userId, UUID currentSessionId);

    // ========== Admin Endpoints ==========

    PaginationResponse<AdminSessionResponse> getAllSessionsAdmin(SessionFilterRequest request);

    void logoutSessionAdmin(UUID sessionId);

    void logoutAllSessionsAdmin(UUID userId);

    // ========== Maintenance ==========

    void expireOldSessions();

    void cleanupOldSessions(int daysOld);
}
