package com.emenu.features.audit.controller;

import com.emenu.features.audit.dto.filter.AuditLogFilterDTO;
import com.emenu.features.audit.dto.response.AuditLogResponseDTO;
import com.emenu.features.audit.dto.response.AuditStatsResponseDTO;
import com.emenu.features.audit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit logging and monitoring endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get all audit logs with filters", description = "Retrieve audit logs with optional filters and pagination")
    public ResponseEntity<Page<AuditLogResponseDTO>> getAuditLogs(
            @ModelAttribute AuditLogFilterDTO filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAuditLogs(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get audit log by ID", description = "Retrieve a single audit log by its ID")
    public ResponseEntity<AuditLogResponseDTO> getAuditLogById(@PathVariable UUID id) {
        return ResponseEntity.ok(auditLogService.getAuditLogById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get user audit logs", description = "Retrieve audit logs for a specific user")
    public ResponseEntity<Page<AuditLogResponseDTO>> getUserAuditLogs(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getUserAuditLogs(userId, pageable));
    }

    @GetMapping("/ip/{ipAddress}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get audit logs by IP address", description = "Retrieve audit logs for a specific IP address")
    public ResponseEntity<Page<AuditLogResponseDTO>> getAuditLogsByIp(
            @PathVariable String ipAddress,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByIp(ipAddress, pageable));
    }

    @GetMapping("/errors")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get error logs", description = "Retrieve audit logs with error status codes (>= 400)")
    public ResponseEntity<Page<AuditLogResponseDTO>> getErrorLogs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getErrorLogs(pageable));
    }

    @GetMapping("/anonymous")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get anonymous access logs", description = "Retrieve audit logs for requests without authentication token")
    public ResponseEntity<Page<AuditLogResponseDTO>> getAnonymousAccessLogs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAnonymousAccessLogs(pageable));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get audit statistics", description = "Retrieve audit logging statistics")
    public ResponseEntity<AuditStatsResponseDTO> getAuditStats() {
        return ResponseEntity.ok(auditLogService.getAuditStats());
    }
}
