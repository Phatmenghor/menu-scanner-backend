package com.emenu.features.audit.controller;

import com.emenu.features.audit.dto.filter.AuditLogFilterDTO;
import com.emenu.features.audit.dto.response.AuditLogResponseDTO;
import com.emenu.features.audit.dto.response.AuditStatsResponseDTO;
import com.emenu.features.audit.service.AuditLogService;
import com.emenu.shared.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaginationResponse<AuditLogResponseDTO>> searchAuditLogs(@RequestBody AuditLogFilterDTO filter) {
        log.debug("Searching audit logs with filter: {}", filter);
        return ResponseEntity.ok(auditLogService.searchAuditLogs(filter));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AuditLogResponseDTO> getAuditLogById(@PathVariable UUID id) {
        log.debug("Getting audit log by id: {}", id);
        return ResponseEntity.ok(auditLogService.getAuditLogById(id));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AuditStatsResponseDTO> getAuditStats() {
        log.debug("Getting audit statistics");
        return ResponseEntity.ok(auditLogService.getAuditStats());
    }
}
