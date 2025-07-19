package com.emenu.security.audit;

import com.emenu.features.audit.service.AuditService;
import com.emenu.features.user_management.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditLogger {

    private final AuditService auditService;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        String username = authentication.getName();
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        auditService.logAuthenticationSuccess(username, ipAddress, userAgent);

        // Update user's last login IP
        userRepository.findByEmailAndIsDeletedFalse(username)
                .ifPresent(user -> {
                    user.setLastLoginIp(ipAddress);
                    userRepository.save(user);
                });
    }

    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();
        String reason = event.getException().getMessage();

        auditService.logAuthenticationFailure(username, ipAddress, userAgent, reason);
    }

    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private String getUserAgent() {
        return request.getHeader("User-Agent");
    }
}