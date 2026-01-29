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

    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.statusCode >= 400 ORDER BY a.createdAt DESC")
    Page<AuditLog> findErrorLogs(Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.userId IS NULL ORDER BY a.createdAt DESC")
    Page<AuditLog> findAnonymousAccessLogs(Pageable pageable);

    /**
     * Find all audit logs with dynamic filtering
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.userId = :userId) " +
           "AND (:userIdentifier IS NULL OR LOWER(a.userIdentifier) LIKE LOWER(CONCAT('%', :userIdentifier, '%'))) " +
           "AND (:userType IS NULL OR a.userType = :userType) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(a.endpoint) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(a.userIdentifier) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(a.ipAddress) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AuditLog> findAllWithFilters(
        @Param("userId") UUID userId,
        @Param("userIdentifier") String userIdentifier,
        @Param("userType") String userType,
        @Param("search") String search,
        Pageable pageable
    );
}
