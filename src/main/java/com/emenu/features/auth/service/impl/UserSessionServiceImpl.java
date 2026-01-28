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
import com.emenu.shared.utils.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final UserSessionMapper sessionMapper;
    private final PaginationMapper paginationMapper;

    @Override
    @Transactional
    public void createSession(User user, RefreshToken refreshToken, HttpServletRequest request) {
        String userAgent = request.getHeader(SecurityConstants.HEADER_USER_AGENT);
        String deviceId = request.getHeader(SecurityConstants.HEADER_DEVICE_ID);
        String deviceName = request.getHeader(SecurityConstants.HEADER_DEVICE_NAME);

        UserAgentParser.ParsedUserAgent parsedUA = UserAgentParser.parse(userAgent);

        // Build helper DTO, then use pure MapStruct mapping
        UserSessionCreateHelper helper = UserSessionCreateHelper.builder()
                .userId(user.getId())
                .refreshTokenId(refreshToken.getId())
                .deviceId(deviceId != null ? deviceId : generateDeviceId(userAgent))
                .deviceName(deviceName)
                .deviceType(detectDeviceType(userAgent))
                .userAgent(userAgent)
                .browser(parsedUA.getBrowserWithVersion())
                .operatingSystem(parsedUA.getOs())
                .ipAddress(ClientIpUtils.getClientIp(request))
                .status(SecurityConstants.SESSION_STATUS_ACTIVE)
                .loginAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .expiresAt(refreshToken.getExpiryDate())
                .isCurrentSession(true)
                .build();

        UserSession session = sessionMapper.createFromHelper(helper);
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
    @Transactional(readOnly = true)
    public List<UserSessionResponse> getAllSessions(UUID userId) {
        return sessionRepository.findAllSessionsByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserSessionResponse getSessionById(UUID sessionId, UUID userId) {
        UserSession session = sessionRepository.findByIdAndUserIdAndIsDeletedFalse(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        return toResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminSessionResponse getSessionByIdAdmin(UUID sessionId) {
        UserSession session = sessionRepository.findByIdAndIsDeletedFalse(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        return toAdminResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<AdminSessionResponse> getAllSessionsAdmin(SessionFilterRequest request) {
        Pageable pageable = PaginationUtils.createPageable(
                request.getPageNo(), request.getPageSize(), request.getSortBy(), request.getSortDirection()
        );

        List<String> statuses = (request.getStatuses() != null && !request.getStatuses().isEmpty())
                ? request.getStatuses() : null;
        List<String> deviceTypes = (request.getDeviceTypes() != null && !request.getDeviceTypes().isEmpty())
                ? request.getDeviceTypes() : null;

        Page<UserSession> page = sessionRepository.findAllWithFilters(
                request.getUserId(),
                statuses,
                deviceTypes,
                request.getSearch(),
                pageable
        );

        List<AdminSessionResponse> content = page.getContent().stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());

        return paginationMapper.toPaginationResponse(page, content);
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

    private AdminSessionResponse toAdminResponse(UserSession session) {
        User user = session.getUser();
        String userFullName = null;
        String userIdentifier = null;
        String userType = null;

        if (user != null) {
            userFullName = (user.getFirstName() != null ? user.getFirstName() : "") +
                    (user.getLastName() != null ? " " + user.getLastName() : "");
            userFullName = userFullName.trim().isEmpty() ? null : userFullName.trim();
            userIdentifier = user.getUserIdentifier();
            userType = user.getUserType() != null ? user.getUserType().name() : null;
        }

        return AdminSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .userIdentifier(userIdentifier)
                .userFullName(userFullName)
                .userType(userType)
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
                .loggedOutAt(session.getLoggedOutAt())
                .logoutReason(session.getLogoutReason())
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
