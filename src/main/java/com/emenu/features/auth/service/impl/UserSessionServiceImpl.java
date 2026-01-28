package com.emenu.features.auth.service.impl;

import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.features.auth.dto.filter.SessionFilterRequest;
import com.emenu.features.auth.dto.helper.UserSessionCreateHelper;
import com.emenu.features.auth.dto.session.AdminSessionResponse;
import com.emenu.features.auth.dto.session.UserSessionResponse;
import com.emenu.features.auth.mapper.UserSessionMapper;
import com.emenu.features.auth.models.RefreshToken;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.models.UserSession;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.repository.UserSessionRepository;
import com.emenu.features.auth.service.UserSessionService;
import com.emenu.shared.constants.SecurityConstants;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.pagination.PaginationUtils;
import com.emenu.shared.utils.ClientIpUtils;
import com.emenu.shared.utils.IpGeolocationService;
import com.emenu.shared.utils.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionServiceImpl implements UserSessionService {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final UserSessionMapper sessionMapper;
    private final PaginationMapper paginationMapper;
    private final IpGeolocationService geolocationService;

    @Override
    @Transactional
    public UserSessionResponse createSession(User user, RefreshToken refreshToken, HttpServletRequest request) {
        String userAgent = request.getHeader(SecurityConstants.HEADER_USER_AGENT);
        String deviceId = request.getHeader(SecurityConstants.HEADER_DEVICE_ID);
        String deviceName = request.getHeader(SecurityConstants.HEADER_DEVICE_NAME);
        String ipAddress = ClientIpUtils.getClientIp(request);

        UserAgentParser.ParsedUserAgent parsedUA = UserAgentParser.parse(userAgent);
        String location = getLocationFromIp(ipAddress);

        UserSessionCreateHelper helper = UserSessionCreateHelper.builder()
                .userId(user.getId())
                .refreshTokenId(refreshToken.getId())
                .deviceId(deviceId != null ? deviceId : generateDeviceId(userAgent))
                .deviceName(deviceName)
                .deviceType(detectDeviceType(userAgent))
                .userAgent(userAgent)
                .browser(parsedUA.getBrowserWithVersion())
                .operatingSystem(parsedUA.getOs())
                .ipAddress(ipAddress)
                .location(location)
                .status(SecurityConstants.SESSION_STATUS_ACTIVE)
                .loginAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .expiresAt(refreshToken.getExpiryDate())
                .isCurrentSession(true)
                .build();

        UserSession session = sessionMapper.createFromHelper(helper);
        sessionRepository.save(session);
        sessionRepository.markOtherSessionsAsNotCurrent(user.getId(), session.getId());

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastActiveAt(LocalDateTime.now());
        user.setActiveSessionsCount(sessionRepository.countActiveSessionsByUserId(user.getId()).intValue());
        userRepository.save(user);

        log.info("Created session for user: {} on device: {} from {}", user.getUserIdentifier(), session.getDeviceDisplayName(), location);
        return sessionMapper.toResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionResponse> getAllSessions(UUID userId) {
        return sessionMapper.toResponseList(sessionRepository.findAllSessionsByUserId(userId));
    }

    @Override
    @Transactional
    public UserSessionResponse logoutSession(UUID sessionId, UUID userId) {
        UserSession session = sessionRepository.findByIdAndUserIdAndIsDeletedFalse(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        session.logout("User logged out");
        sessionRepository.save(session);
        updateActiveSessionsCount(userId);

        log.info("User {} logged out from session {}", userId, sessionId);
        return sessionMapper.toResponse(session);
    }

    @Override
    @Transactional
    public List<UserSessionResponse> logoutOtherSessions(UUID userId, UUID currentSessionId) {
        List<UserSession> sessions = sessionRepository.findActiveSessionsByUserId(userId);
        List<UserSession> loggedOutSessions = new ArrayList<>();

        for (UserSession session : sessions) {
            if (!session.getId().equals(currentSessionId)) {
                session.logout("Logged out from other device");
                sessionRepository.save(session);
                loggedOutSessions.add(session);
            }
        }

        updateActiveSessionsCount(userId);
        log.info("User {} logged out from {} other devices", userId, loggedOutSessions.size());
        return sessionMapper.toResponseList(loggedOutSessions);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<AdminSessionResponse> getAllSessionsAdmin(SessionFilterRequest request) {
        Pageable pageable = PaginationUtils.createPageable(
                request.getPageNo(), request.getPageSize(), request.getSortBy(), request.getSortDirection()
        );

        List<String> statuses = (request.getStatuses() != null && !request.getStatuses().isEmpty()) ? request.getStatuses() : null;
        List<String> deviceTypes = (request.getDeviceTypes() != null && !request.getDeviceTypes().isEmpty()) ? request.getDeviceTypes() : null;

        Page<UserSession> page = sessionRepository.findAllWithFilters(
                request.getUserId(), statuses, deviceTypes, request.getSearch(), pageable
        );

        return sessionMapper.toPaginationResponse(page, paginationMapper);
    }

    @Override
    @Transactional
    public AdminSessionResponse logoutSessionAdmin(UUID sessionId) {
        UserSession session = sessionRepository.findByIdAndIsDeletedFalse(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (session.isActive()) {
            session.logout("Admin logged out");
            sessionRepository.save(session);
            updateActiveSessionsCount(session.getUserId());
            log.info("Admin logged out session {}", sessionId);
        }

        return sessionMapper.toAdminResponse(session);
    }

    @Override
    @Transactional
    public List<AdminSessionResponse> logoutAllSessionsAdmin(UUID userId) {
        List<UserSession> sessions = sessionRepository.findActiveSessionsByUserId(userId);

        for (UserSession session : sessions) {
            session.logout("Admin logged out all");
            sessionRepository.save(session);
        }

        updateActiveSessionsCount(userId);
        log.info("Admin logged out {} sessions for user {}", sessions.size(), userId);
        return sessionMapper.toAdminResponseList(sessions);
    }

    private void updateActiveSessionsCount(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setActiveSessionsCount(sessionRepository.countActiveSessionsByUserId(userId).intValue());
            userRepository.save(user);
        }
    }

    private String generateDeviceId(String userAgent) {
        return UUID.nameUUIDFromBytes((userAgent != null ? userAgent : "unknown").getBytes()).toString();
    }

    private String detectDeviceType(String userAgent) {
        if (userAgent == null) return SecurityConstants.DEVICE_TYPE_UNKNOWN;
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile")) return SecurityConstants.DEVICE_TYPE_MOBILE;
        if (ua.contains("tablet") || ua.contains("ipad")) return SecurityConstants.DEVICE_TYPE_TABLET;
        return SecurityConstants.DEVICE_TYPE_DESKTOP;
    }

    private String getLocationFromIp(String ipAddress) {
        try {
            IpGeolocationService.GeoLocation geo = geolocationService.getLocation(ipAddress);
            return geo.getLocationDisplay();
        } catch (Exception e) {
            log.warn("Failed to get location for IP: {}", ipAddress);
            return "Unknown";
        }
    }
}
