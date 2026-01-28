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

    // ========== Session Creation ==========

    UserSessionResponse createSession(User user, RefreshToken refreshToken, HttpServletRequest request);

    // ========== User Endpoints ==========

    List<UserSessionResponse> getAllSessions(UUID userId);

    UserSessionResponse logoutSession(UUID sessionId, UUID userId);

    List<UserSessionResponse> logoutOtherSessions(UUID userId, UUID currentSessionId);

    // ========== Admin Endpoints ==========

    PaginationResponse<AdminSessionResponse> getAllSessionsAdmin(SessionFilterRequest request);

    AdminSessionResponse logoutSessionAdmin(UUID sessionId);

    List<AdminSessionResponse> logoutAllSessionsAdmin(UUID userId);
}
