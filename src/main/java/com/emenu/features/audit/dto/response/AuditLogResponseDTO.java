package com.emenu.features.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDTO {

    private UUID id;
    private UUID userId;
    private String userIdentifier;
    private String userType;
    private String httpMethod;
    private String endpoint;
    private String ipAddress;
    private String userAgent;
    private Integer statusCode;
    private Long responseTimeMs;
    private String errorMessage;
    private String sessionId;
    private String requestParams;
    private String requestBody;
    private LocalDateTime createdAt;
}
