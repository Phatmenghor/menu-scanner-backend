package com.emenu.features.audit.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Helper DTO for creating AuditLog via MapStruct
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogCreateHelper {
    private UUID userId;
    private String userIdentifier;
    private String userType;
    private String httpMethod;
    private String endpoint;
    private String ipAddress;
    private String userAgent;
    private String requestParams;
    private String requestBody;
    private Integer statusCode;
    private Long responseTimeMs;
    private String errorMessage;
    private String sessionId;
}
