package com.emenu.features.audit.service;

import com.emenu.features.audit.domain.AuditLog;
import com.emenu.features.user_management.models.User;

import java.util.UUID;

public interface AuditService {
    
    void logUserAction(User user, String action, String entityType, UUID entityId, String description);
    void logUserAction(User user, String action, String entityType, UUID entityId, String description, Object oldValues, Object newValues);
    void logAuthenticationSuccess(String email, String ipAddress, String userAgent);
    void logAuthenticationFailure(String email, String ipAddress, String userAgent, String reason);
    void logPasswordChange(User user, String ipAddress);
    void logAccountLock(User user, String reason);
    void logAccountUnlock(User user, String reason);
    void logBusinessAction(User user, String action, UUID businessId, String description);
    void logSecurityEvent(String event, String description, String severity);
    void logError(User user, String action, String entityType, UUID entityId, String errorMessage, Exception exception);
    
    AuditLog createAuditLog(String userEmail, String action, String entityType, UUID entityId, String description);
}
