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

    /**
     * Find all audit logs with dynamic filtering
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.userId = :userId) " +
           "AND (:userIdentifier IS NULL OR LOWER(a.userIdentifier) LIKE LOWER(CONCAT('%', :userIdentifier, '%'))) " +
           "AND (:userType IS NULL OR a.userType = :userType) " +
           "AND (:httpMethod IS NULL OR a.httpMethod = :httpMethod) " +
           "AND (:endpoint IS NULL OR LOWER(a.endpoint) LIKE LOWER(CONCAT('%', :endpoint, '%'))) " +
           "AND (:ipAddress IS NULL OR a.ipAddress = :ipAddress) " +
           "AND (:statusCode IS NULL OR a.statusCode = :statusCode) " +
           "AND (:minStatusCode IS NULL OR a.statusCode >= :minStatusCode) " +
           "AND (:maxStatusCode IS NULL OR a.statusCode <= :maxStatusCode) " +
           "AND (:startDate IS NULL OR a.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR a.createdAt <= :endDate) " +
           "AND (:minResponseTime IS NULL OR a.responseTimeMs >= :minResponseTime) " +
           "AND (:maxResponseTime IS NULL OR a.responseTimeMs <= :maxResponseTime) " +
           "AND (:hasError IS NULL OR (:hasError = true AND a.statusCode >= 400) OR (:hasError = false AND a.statusCode < 400)) " +
           "AND (:isAnonymous IS NULL OR (:isAnonymous = true AND a.userId IS NULL) OR (:isAnonymous = false AND a.userId IS NOT NULL)) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(a.endpoint) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(a.userIdentifier) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(a.ipAddress) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AuditLog> findAllWithFilters(
        @Param("userId") UUID userId,
        @Param("userIdentifier") String userIdentifier,
        @Param("userType") String userType,
        @Param("httpMethod") String httpMethod,
        @Param("endpoint") String endpoint,
        @Param("ipAddress") String ipAddress,
        @Param("statusCode") Integer statusCode,
        @Param("minStatusCode") Integer minStatusCode,
        @Param("maxStatusCode") Integer maxStatusCode,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("minResponseTime") Long minResponseTime,
        @Param("maxResponseTime") Long maxResponseTime,
        @Param("hasError") Boolean hasError,
        @Param("isAnonymous") Boolean isAnonymous,
        @Param("search") String search,
        Pageable pageable
    );
}
