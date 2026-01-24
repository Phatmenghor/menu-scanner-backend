package com.emenu.shared.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<AuditLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress);

    List<AuditLog> findByEndpointContainingOrderByCreatedAtDesc(String endpoint);

    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}
