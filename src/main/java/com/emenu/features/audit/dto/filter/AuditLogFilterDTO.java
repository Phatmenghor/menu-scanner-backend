package com.emenu.features.audit.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogFilterDTO {

    private UUID userId;
    private String userIdentifier;
    private String userType;
    private String httpMethod;
    private String endpoint;
    private String ipAddress;
    private Integer statusCode;
    private Integer minStatusCode;
    private Integer maxStatusCode;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long minResponseTime;
    private Long maxResponseTime;
    private Boolean hasError;
    private Boolean isAnonymous;
}
