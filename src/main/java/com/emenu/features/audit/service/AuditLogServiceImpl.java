package com.emenu.features.audit.service;

import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.features.audit.dto.filter.AuditLogFilterDTO;
import com.emenu.features.audit.dto.response.AuditLogResponseDTO;
import com.emenu.features.audit.dto.response.AuditStatsResponseDTO;
import com.emenu.features.audit.models.AuditLog;
import com.emenu.features.audit.repository.AuditLogRepository;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.utils.ClientIpUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityUtils securityUtils;
    private final PaginationMapper paginationMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<AuditLogResponseDTO> searchAuditLogs(AuditLogFilterDTO filter) {
        Sort sort = Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPageNo() - 1, filter.getPageSize(), sort);

        Specification<AuditLog> spec = buildSpecification(filter);
        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);

        List<AuditLogResponseDTO> content = page.getContent().stream()
                .map(this::toResponseDTO)
                .toList();

        return paginationMapper.toPaginationResponse(page, content);
    }

    @Override
    @Async
    public void logAccess(HttpServletRequest request, int statusCode, long responseTimeMs, String errorMessage) {
        logAccessWithBodies(request, statusCode, responseTimeMs, errorMessage, null, null);
    }

    @Override
    @Async
    @Transactional
    public void logAccessWithBodies(HttpServletRequest request, int statusCode, long responseTimeMs,
                                   String errorMessage, String requestBody, String responseBody) {
        try {
            AuditLog auditLog = new AuditLog();

            // Try to get user info from security context
            try {
                UUID userId = securityUtils.getCurrentUserId();
                String userIdentifier = securityUtils.getCurrentUserIdentifier();
                UserType userType = securityUtils.getCurrentUserType();

                auditLog.setUserId(userId);
                auditLog.setUserIdentifier(userIdentifier);
                auditLog.setUserType(userType != null ? userType.name() : null);
            } catch (Exception e) {
                // User not authenticated - log as anonymous
                auditLog.setUserId(null);
                auditLog.setUserIdentifier("anonymous");
                auditLog.setUserType("ANONYMOUS");
            }

            // Request info
            auditLog.setHttpMethod(request.getMethod());
            auditLog.setEndpoint(request.getRequestURI());
            auditLog.setIpAddress(ClientIpUtils.getClientIp(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));

            // Request parameters
            if (!request.getParameterMap().isEmpty()) {
                auditLog.setRequestParams(buildParamsString(request.getParameterMap()));
            }

            // Request/Response bodies (optional - can be null)
            if (requestBody != null && requestBody.length() > 0) {
                // Limit to 10000 chars to avoid DB issues
                auditLog.setRequestBody(requestBody.length() > 10000 ?
                    requestBody.substring(0, 10000) + "... [truncated]" : requestBody);
            }

            if (responseBody != null && responseBody.length() > 0) {
                // Limit to 10000 chars to avoid DB issues
                auditLog.setResponseBody(responseBody.length() > 10000 ?
                    responseBody.substring(0, 10000) + "... [truncated]" : responseBody);
            }

            // Response info
            auditLog.setStatusCode(statusCode);
            auditLog.setResponseTimeMs(responseTimeMs);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setSessionId(request.getSession(false) != null ?
                    request.getSession().getId() : null);

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> getAuditLogs(AuditLogFilterDTO filter, Pageable pageable) {
        Specification<AuditLog> spec = buildSpecification(filter);
        return auditLogRepository.findAll(spec, pageable)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> getUserAuditLogs(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> getAuditLogsByIp(String ipAddress, Pageable pageable) {
        return auditLogRepository.findByIpAddressOrderByCreatedAtDesc(ipAddress, pageable)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> getErrorLogs(Pageable pageable) {
        return auditLogRepository.findErrorLogs(pageable)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> getAnonymousAccessLogs(Pageable pageable) {
        return auditLogRepository.findAnonymousAccessLogs(pageable)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditStatsResponseDTO getAuditStats() {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);

        long totalLogs = auditLogRepository.count();
        long last24HoursCount = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                last24Hours, LocalDateTime.now(), Pageable.unpaged()).getTotalElements();
        long last7DaysCount = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                last7Days, LocalDateTime.now(), Pageable.unpaged()).getTotalElements();

        AuditStatsResponseDTO stats = new AuditStatsResponseDTO();
        stats.setTotalLogs(totalLogs);
        stats.setLast24Hours(last24HoursCount);
        stats.setLast7Days(last7DaysCount);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponseDTO getAuditLogById(UUID id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found with id: " + id));
        return toResponseDTO(auditLog);
    }

    private Specification<AuditLog> buildSpecification(AuditLogFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), filter.getUserId()));
            }

            if (filter.getUserIdentifier() != null && !filter.getUserIdentifier().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("userIdentifier")),
                        "%" + filter.getUserIdentifier().toLowerCase() + "%"));
            }

            if (filter.getUserType() != null && !filter.getUserType().isEmpty()) {
                predicates.add(cb.equal(root.get("userType"), filter.getUserType()));
            }

            if (filter.getHttpMethod() != null && !filter.getHttpMethod().isEmpty()) {
                predicates.add(cb.equal(root.get("httpMethod"), filter.getHttpMethod()));
            }

            if (filter.getEndpoint() != null && !filter.getEndpoint().isEmpty()) {
                predicates.add(cb.like(root.get("endpoint"), "%" + filter.getEndpoint() + "%"));
            }

            if (filter.getIpAddress() != null && !filter.getIpAddress().isEmpty()) {
                predicates.add(cb.equal(root.get("ipAddress"), filter.getIpAddress()));
            }

            if (filter.getStatusCode() != null) {
                predicates.add(cb.equal(root.get("statusCode"), filter.getStatusCode()));
            }

            if (filter.getMinStatusCode() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("statusCode"), filter.getMinStatusCode()));
            }

            if (filter.getMaxStatusCode() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("statusCode"), filter.getMaxStatusCode()));
            }

            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));
            }

            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));
            }

            if (filter.getMinResponseTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("responseTimeMs"), filter.getMinResponseTime()));
            }

            if (filter.getMaxResponseTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("responseTimeMs"), filter.getMaxResponseTime()));
            }

            if (filter.getHasError() != null && filter.getHasError()) {
                predicates.add(cb.isNotNull(root.get("errorMessage")));
            }

            if (filter.getIsAnonymous() != null && filter.getIsAnonymous()) {
                predicates.add(cb.isNull(root.get("userId")));
            }

            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private AuditLogResponseDTO toResponseDTO(AuditLog auditLog) {
        AuditLogResponseDTO dto = new AuditLogResponseDTO();
        dto.setId(auditLog.getId());
        dto.setUserId(auditLog.getUserId());
        dto.setUserIdentifier(auditLog.getUserIdentifier());
        dto.setUserType(auditLog.getUserType());
        dto.setHttpMethod(auditLog.getHttpMethod());
        dto.setEndpoint(auditLog.getEndpoint());
        dto.setIpAddress(auditLog.getIpAddress());
        dto.setUserAgent(auditLog.getUserAgent());
        dto.setStatusCode(auditLog.getStatusCode());
        dto.setResponseTimeMs(auditLog.getResponseTimeMs());
        dto.setErrorMessage(auditLog.getErrorMessage());
        dto.setSessionId(auditLog.getSessionId());
        dto.setRequestParams(auditLog.getRequestParams());
        dto.setRequestBody(auditLog.getRequestBody());
        dto.setResponseBody(auditLog.getResponseBody());
        dto.setCreatedAt(auditLog.getCreatedAt());
        return dto;
    }

    private String buildParamsString(Map<String, String[]> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=");
            if (entry.getValue() != null && entry.getValue().length > 0) {
                sb.append(String.join(",", entry.getValue()));
            }
        }

        String result = sb.toString();
        // Limit to 2000 chars
        return result.length() > 2000 ? result.substring(0, 2000) + "... [truncated]" : result;
    }
}
