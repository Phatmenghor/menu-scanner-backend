package com.emenu.features.audit.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogFilterDTO extends BaseFilterRequest {

    // User filters
    private UUID userId;
    private String userIdentifier;
    private String userType;

    // Request filters
    private String httpMethod;
    private String endpoint;
    private String ipAddress;

    // Status filters
    private Integer statusCode;
    private Integer minStatusCode;
    private Integer maxStatusCode;

    // Time filters
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Performance filters
    private Long minResponseTime;
    private Long maxResponseTime;

    // Boolean filters
    private Boolean hasError;
    private Boolean isAnonymous;
}
