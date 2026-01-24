package com.emenu.features.auth.service.impl;

import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.features.auth.dto.session.UserSessionResponse;
import com.emenu.features.auth.models.RefreshToken;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.models.UserSession;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.repository.UserSessionRepository;
import com.emenu.features.auth.service.UserSessionService;
import com.emenu.shared.constants.SecurityConstants;
import com.emenu.shared.utils.ClientIpUtils;
import com.emenu.shared.utils.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionServiceImpl implements UserSessionService {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createSession(User user, RefreshToken refreshToken, HttpServletRequest request) {
        String userAgent = request.getHeader(SecurityConstants.HEADER_USER_AGENT);
        String deviceId = request.getHeader(SecurityConstants.HEADER_DEVICE_ID);
        String deviceName = request.getHeader(SecurityConstants.HEADER_DEVICE_NAME);

        UserAgentParser.ParsedUserAgent parsedUA = UserAgentParser.parse(userAgent);

        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setRefreshTokenId(refreshToken.getId());
        session.setDeviceId(deviceId != null ? deviceId : generateDeviceId(userAgent));
        session.setDeviceName(deviceName);
        session.setDeviceType(detectDeviceType(userAgent));
        session.setUserAgent(userAgent);
        session.setBrowser(parsedUA.getBrowserWithVersion());
        session.setOperatingSystem(parsedUA.getOs());
        session.setIpAddress(ClientIpUtils.getClientIp(request));
        session.setStatus(SecurityConstants.SESSION_STATUS_ACTIVE);
        session.setLoginAt(LocalDateTime.now());
        session.setLastActiveAt(LocalDateTime.now());
        session.setExpiresAt(refreshToken.getExpiryDate());
        session.setIsCurrentSession(true);

        sessionRepository.save(session);
        sessionRepository.markOtherSessionsAsNotCurrent(user.getId(), session.getId());

        // Update user login time and active sessions count
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastActiveAt(LocalDateTime.now());
        user.setActiveSessionsCount(sessionRepository.countActiveSessionsByUserId(user.getId()).intValue());
        userRepository.save(user);

        log.info("Created session for user: {} on device: {}", user.getUserIdentifier(), session.getDeviceDisplayName());
    }

    @Override
    @Transactional
    public void updateLastActive(UUID userId, String deviceId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setLastActiveAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionResponse> getActiveSessions(UUID userId) {
        return sessionRepository.findActiveSessionsByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionResponse> getAllSessions(UUID userId) {
        return sessionRepository.findAllSessionsByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void logoutSession(UUID sessionId, UUID userId) {
        int updated = sessionRepository.logoutSession(sessionId, userId, LocalDateTime.now(), "User logged out from this device");
        if (updated > 0) {
            updateActiveSessionsCount(userId);
            log.info("User {} logged out from session {}", userId, sessionId);
        }
    }

    @Override
    @Transactional
    public void logoutAllOtherDevices(UUID userId, UUID currentSessionId) {
        List<UserSession> sessions = sessionRepository.findActiveSessionsByUserId(userId);
        for (UserSession session : sessions) {
            if (!session.getId().equals(currentSessionId)) {
                session.logout("Logged out from other device");
                sessionRepository.save(session);
            }
        }
        updateActiveSessionsCount(userId);
        log.info("User {} logged out from all other devices", userId);
    }

    @Override
    @Transactional
    public void logoutAllDevices(UUID userId) {
        sessionRepository.logoutAllSessionsByUserId(userId, LocalDateTime.now(), "Logged out from all devices");
        updateActiveSessionsCount(userId);
        log.info("User {} logged out from all devices", userId);
    }

    @Override
    @Transactional
    public void expireOldSessions() {
        List<UserSession> expiredSessions = sessionRepository.findExpiredSessions(LocalDateTime.now());
        for (UserSession session : expiredSessions) {
            session.expire();
            sessionRepository.save(session);
            updateActiveSessionsCount(session.getUserId());
        }
        log.info("Expired {} old sessions", expiredSessions.size());
    }

    @Override
    @Transactional
    public void cleanupOldSessions(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deleted = sessionRepository.cleanupOldSessions(LocalDateTime.now(), cutoffDate);
        log.info("Cleaned up {} old sessions", deleted);
    }

    private void updateActiveSessionsCount(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setActiveSessionsCount(sessionRepository.countActiveSessionsByUserId(userId).intValue());
            userRepository.save(user);
        }
    }

    private UserSessionResponse toResponse(UserSession session) {
        return UserSessionResponse.builder()
                .id(session.getId())
                .deviceId(session.getDeviceId())
                .deviceName(session.getDeviceName())
                .deviceType(session.getDeviceType())
                .deviceDisplayName(session.getDeviceDisplayName())
                .browser(session.getBrowser())
                .operatingSystem(session.getOperatingSystem())
                .ipAddress(session.getIpAddress())
                .country(session.getCountry())
                .city(session.getCity())
                .status(session.getStatus())
                .loginAt(session.getLoginAt())
                .lastActiveAt(session.getLastActiveAt())
                .expiresAt(session.getExpiresAt())
                .isCurrentSession(session.getIsCurrentSession())
                .sessionDurationMinutes(session.getSessionDurationMinutes())
                .inactiveDurationMinutes(session.getInactiveDurationMinutes())
                .build();
    }

    private String generateDeviceId(String userAgent) {
        return UUID.nameUUIDFromBytes(userAgent.getBytes()).toString();
    }

    private String detectDeviceType(String userAgent) {
        if (userAgent == null) return SecurityConstants.DEVICE_TYPE_UNKNOWN;
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile")) return SecurityConstants.DEVICE_TYPE_MOBILE;
        if (ua.contains("tablet") || ua.contains("ipad")) return SecurityConstants.DEVICE_TYPE_TABLET;
        return SecurityConstants.DEVICE_TYPE_DESKTOP;
    }
}
