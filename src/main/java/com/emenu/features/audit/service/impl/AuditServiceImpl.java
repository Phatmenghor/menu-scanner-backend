package com.emenu.features.audit.service.impl;

import com.emenu.features.audit.domain.AuditLog;
import com.emenu.features.audit.repository.AuditLogRepository;
import com.emenu.features.audit.service.AuditService;
import com.emenu.features.auth.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;

    @Override
    @Async
    public void logUserAction(User user, String action, String entityType, UUID entityId, String description) {
        logUserAction(user, action, entityType, entityId, description, null, null);
    }

    @Override
    @Async
    public void logUserAction(User user, String action, String entityType, UUID entityId, String description, Object oldValues, Object newValues) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(user.getId());
            auditLog.setUserEmail(user.getEmail());
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setAction(action);
            auditLog.setDescription(description);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setIpAddress(getClientIpAddress());
            auditLog.setUserAgent(getUserAgent());
            auditLog.setBusinessId(user.getBusinessId());
            auditLog.setCategory("USER_MANAGEMENT");
            auditLog.setSuccess(true);

            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }
            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }

            auditLogRepository.save(auditLog);
            log.debug("Audit log created for user {} action {}", user.getEmail(), action);
        } catch (Exception e) {
            log.error("Failed to create audit log for user {} action {}", user.getEmail(), action, e);
        }
    }

    @Override
    @Async
    public void logAuthenticationSuccess(String email, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserEmail(email);
            auditLog.setEntityType("USER");
            auditLog.setAction("LOGIN_SUCCESS");
            auditLog.setDescription("User successfully logged in");
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setCategory("AUTHENTICATION");
            auditLog.setSuccess(true);

            auditLogRepository.save(auditLog);
            log.debug("Authentication success logged for user {}", email);
        } catch (Exception e) {
            log.error("Failed to log authentication success for user {}", email, e);
        }
    }

    @Override
    @Async
    public void logAuthenticationFailure(String email, String ipAddress, String userAgent, String reason) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserEmail(email);
            auditLog.setEntityType("USER");
            auditLog.setAction("LOGIN_FAILURE");
            auditLog.setDescription("User login failed: " + reason);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setCategory("AUTHENTICATION");
            auditLog.setSeverity("WARN");
            auditLog.setSuccess(false);
            auditLog.setErrorMessage(reason);

            auditLogRepository.save(auditLog);
            log.debug("Authentication failure logged for user {}", email);
        } catch (Exception e) {
            log.error("Failed to log authentication failure for user {}", email, e);
        }
    }

    @Override
    @Async
    public void logPasswordChange(User user, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(user.getId());
            auditLog.setUserEmail(user.getEmail());
            auditLog.setEntityType("USER");
            auditLog.setEntityId(user.getId());
            auditLog.setAction("PASSWORD_CHANGE");
            auditLog.setDescription("User changed password");
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(getUserAgent());
            auditLog.setCategory("SECURITY");
            auditLog.setSeverity("INFO");
            auditLog.setSuccess(true);

            auditLogRepository.save(auditLog);
            log.debug("Password change logged for user {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to log password change for user {}", user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void logAccountLock(User user, String reason) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(user.getId());
            auditLog.setUserEmail(user.getEmail());
            auditLog.setEntityType("USER");
            auditLog.setEntityId(user.getId());
            auditLog.setAction("ACCOUNT_LOCKED");
            auditLog.setDescription("User account locked: " + reason);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setIpAddress(getClientIpAddress());
            auditLog.setUserAgent(getUserAgent());
            auditLog.setCategory("SECURITY");
            auditLog.setSeverity("WARN");
            auditLog.setSuccess(true);

            auditLogRepository.save(auditLog);
            log.debug("Account lock logged for user {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to log account lock for user {}", user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void logAccountUnlock(User user, String reason) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(user.getId());
            auditLog.setUserEmail(user.getEmail());
            auditLog.setEntityType("USER");
            auditLog.setEntityId(user.getId());
            auditLog.setAction("ACCOUNT_UNLOCKED");
            auditLog.setDescription("User account unlocked: " + reason);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setIpAddress(getClientIpAddress());
            auditLog.setUserAgent(getUserAgent());
            auditLog.setCategory("SECURITY");
            auditLog.setSeverity("INFO");
            auditLog.setSuccess(true);

            auditLogRepository.save(auditLog);
            log.debug("Account unlock logged for user {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to log account unlock for user {}", user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void logBusinessAction(User user, String action, UUID businessId, String description) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(user.getId());
            auditLog.setUserEmail(user.getEmail());
            auditLog.setEntityType("BUSINESS");
            auditLog.setEntityId(businessId);
            auditLog.setAction(action);
            auditLog.setDescription(description);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setIpAddress(getClientIpAddress());
            auditLog.setUserAgent(getUserAgent());
            auditLog.setBusinessId(businessId);
            auditLog.setCategory("BUSINESS_OPERATION");
            auditLog.setSuccess(true);

            auditLogRepository.save(auditLog);
            log.debug("Business action {} logged for user {}", action, user.getEmail());
        } catch (Exception e) {
            log.error("Failed to log business action {} for user {}", action, user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void logSecurityEvent(String event, String description, String severity) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEntityType("SYSTEM");
            auditLog.setAction(event);
            auditLog.setDescription(description);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setIpAddress(getClientIpAddress());
            auditLog.setUserAgent(getUserAgent());
            auditLog.setCategory("SECURITY");
            auditLog.setSeverity(severity);
            auditLog.setSuccess(true);

            auditLogRepository.save(auditLog);
            log.debug("Security event {} logged with severity {}", event, severity);
        } catch (Exception e) {
            log.error("Failed to log security event {}", event, e);
        }
    }

    @Override
    @Async
    public void logError(User user, String action, String entityType, UUID entityId, String errorMessage, Exception exception) {
        try {
            AuditLog auditLog = new AuditLog();
            if (user != null) {
                auditLog.setUserId(user.getId());
                auditLog.setUserEmail(user.getEmail());
                auditLog.setBusinessId(user.getBusinessId());
            }
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setAction(action);
            auditLog.setDescription("Error occurred during " + action);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setIpAddress(getClientIpAddress());
            auditLog.setUserAgent(getUserAgent());
            auditLog.setCategory("ERROR");
            auditLog.setSeverity("ERROR");
            auditLog.setSuccess(false);
            auditLog.setErrorMessage(errorMessage);

            if (exception != null) {
                auditLog.setAdditionalData(exception.getClass().getSimpleName() + ": " + exception.getMessage());
            }

            auditLogRepository.save(auditLog);
            log.debug("Error logged for action {} by user {}", action, user != null ? user.getEmail() : "UNKNOWN");
        } catch (Exception e) {
            log.error("Failed to log error for action {}", action, e);
        }
    }

    @Override
    public AuditLog createAuditLog(String userEmail, String action, String entityType, UUID entityId, String description) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserEmail(userEmail);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setDescription(description);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setIpAddress(getClientIpAddress());
        auditLog.setUserAgent(getUserAgent());
        auditLog.setSuccess(true);

        return auditLogRepository.save(auditLog);
    }

    private String getClientIpAddress() {
        try {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getUserAgent() {
        try {
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            return "unknown";
        }
    }
}