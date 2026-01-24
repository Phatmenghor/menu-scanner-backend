package com.emenu.shared.audit;

import com.emenu.security.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityUtils securityUtils;

    @Async
    public void logAccess(HttpServletRequest request, int statusCode, long responseTimeMs, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog();

            try {
                UUID userId = securityUtils.getCurrentUserId();
                auditLog.setUserId(userId);
                auditLog.setUserIdentifier(securityUtils.getCurrentUserIdentifier());
                auditLog.setUserType(securityUtils.getCurrentUserType() != null ?
                        securityUtils.getCurrentUserType().name() : null);
            } catch (Exception e) {
                auditLog.setUserId(null);
                auditLog.setUserIdentifier("anonymous");
            }

            auditLog.setHttpMethod(request.getMethod());
            auditLog.setEndpoint(request.getRequestURI());
            auditLog.setIpAddress(getClientIp(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setStatusCode(statusCode);
            auditLog.setResponseTimeMs(responseTimeMs);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setSessionId(request.getSession(false) != null ?
                    request.getSession().getId() : null);

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
