package com.emenu.features.audit.repository;

import com.emenu.features.audit.models.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<AuditLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress, Pageable pageable);

    Page<AuditLog> findByEndpointContainingOrderByCreatedAtDesc(String endpoint, Pageable pageable);

    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<AuditLog> findByStatusCodeOrderByCreatedAtDesc(Integer statusCode, Pageable pageable);

    Page<AuditLog> findByUserTypeOrderByCreatedAtDesc(String userType, Pageable pageable);

    Page<AuditLog> findByHttpMethodOrderByCreatedAtDesc(String httpMethod, Pageable pageable);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.createdAt > :since")
    Long countByUserIdSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.ipAddress = :ipAddress AND a.createdAt > :since")
    Long countByIpAddressSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    @Query("SELECT a.endpoint, COUNT(a) as count FROM AuditLog a WHERE a.createdAt > :since GROUP BY a.endpoint ORDER BY count DESC")
    List<Object[]> findTopEndpointsSince(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.statusCode >= 400 ORDER BY a.createdAt DESC")
    Page<AuditLog> findErrorLogs(Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.userId IS NULL ORDER BY a.createdAt DESC")
    Page<AuditLog> findAnonymousAccessLogs(Pageable pageable);
}
