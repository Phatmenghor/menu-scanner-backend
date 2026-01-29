package com.emenu.features.audit.service.impl;

import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.features.audit.dto.helper.AuditLogCreateHelper;
import com.emenu.features.audit.dto.filter.AuditLogFilterDTO;
import com.emenu.features.audit.dto.response.AuditLogResponseDTO;
import com.emenu.features.audit.dto.response.AuditStatsResponseDTO;
import com.emenu.features.audit.mapper.AuditLogMapper;
import com.emenu.features.audit.models.AuditLog;
import com.emenu.features.audit.repository.AuditLogRepository;
import com.emenu.features.audit.service.AuditLogService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.utils.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityUtils securityUtils;
    private final PaginationMapper paginationMapper;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<AuditLogResponseDTO> searchAuditLogs(AuditLogFilterDTO filter) {
        Sort sort = Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPageNo() - 1, filter.getPageSize(), sort);

        Page<AuditLog> page = auditLogRepository.findAllWithFilters(
                filter.getUserId(),
                filter.getUserIdentifier(),
                filter.getUserType(),
                filter.getSearch(),
                pageable
        );

        List<AuditLogResponseDTO> content = page.getContent().stream()
                .map(auditLogMapper::toResponseDTO)
                .collect(Collectors.toList());

        return paginationMapper.toPaginationResponse(page, content);
    }

    @Override
    public void logAccessWithBodies(HttpServletRequest request, int statusCode, long responseTimeMs,
                                   String errorMessage, String requestBody) {
        UUID userId = null;
        String userIdentifier = "anonymous";
        String userType = "ANONYMOUS";

        // Try to get user info from security context (must be done synchronously)
        try {
            userId = securityUtils.getCurrentUserId();
            userIdentifier = securityUtils.getCurrentUserIdentifier();
            UserType type = securityUtils.getCurrentUserType();
            userType = type != null ? type.name() : "ANONYMOUS";
        } catch (Exception e) {
            // User not authenticated - use defaults set above
        }

        // Extract request data synchronously (before request is recycled)
        String httpMethod = request.getMethod();
        String endpoint = request.getRequestURI();
        String ipAddress = ClientIpUtils.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String requestParams = !request.getParameterMap().isEmpty() ? buildParamsString(request.getParameterMap()) : null;
        String sessionId = null;
        try {
            sessionId = request.getSession(false) != null ? request.getSession().getId() : null;
        } catch (Exception e) {
            // Session may not be available
        }

        // Build the helper DTO with all extracted data
        final AuditLogCreateHelper helper = buildAuditLogHelper(
                userId, userIdentifier, userType, httpMethod, endpoint, ipAddress,
                userAgent, requestParams, statusCode, responseTimeMs, errorMessage,
                sessionId, requestBody);

        // Now save asynchronously - all data is already extracted
        saveAuditLogAsync(helper);
    }

    private AuditLogCreateHelper buildAuditLogHelper(UUID userId, String userIdentifier, String userType,
                                                      String httpMethod, String endpoint, String ipAddress,
                                                      String userAgent, String requestParams, int statusCode,
                                                      long responseTimeMs, String errorMessage, String sessionId,
                                                      String requestBody) {
        // Truncate request/response bodies if needed
        String truncatedRequestBody = null;
        if (requestBody != null && !requestBody.isEmpty()) {
            truncatedRequestBody = requestBody.length() > 10000 ?
                requestBody.substring(0, 10000) + "... [truncated]" : requestBody;
        }

        return AuditLogCreateHelper.builder()
                .userId(userId)
                .userIdentifier(userIdentifier)
                .userType(userType)
                .httpMethod(httpMethod)
                .endpoint(endpoint)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .requestParams(requestParams)
                .requestBody(truncatedRequestBody)
                .statusCode(statusCode)
                .responseTimeMs(responseTimeMs)
                .errorMessage(errorMessage)
                .sessionId(sessionId)
                .build();
    }

    @Async
    @Transactional
    public void saveAuditLogAsync(AuditLogCreateHelper helper) {
        try {
            AuditLog auditLog = auditLogMapper.createFromHelper(helper);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
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
        return auditLogMapper.toResponseDTO(auditLog);
    }

    private String buildParamsString(Map<String, String[]> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            if (!sb.isEmpty()) {
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
