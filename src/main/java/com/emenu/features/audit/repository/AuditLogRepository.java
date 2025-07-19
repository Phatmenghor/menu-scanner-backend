package com.emenu.features.audit.repository;

import com.emenu.features.audit.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);
    
    Page<AuditLog> findByBusinessIdOrderByTimestampDesc(UUID businessId, Pageable pageable);
    
    List<AuditLog> findByUserEmailAndActionInOrderByTimestampDesc(String userEmail, List<String> actions);
    
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.category = :category AND a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    List<AuditLog> findByCategoryAndTimestampBetween(@Param("category") String category, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT a FROM AuditLog a WHERE a.severity IN :severities ORDER BY a.timestamp DESC")
    Page<AuditLog> findBySeverityIn(@Param("severities") List<String> severities, Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action AND a.timestamp > :since")
    long countByActionSince(@Param("action") String action, @Param("since") LocalDateTime since);
    
    @Query("SELECT a.ipAddress, COUNT(a) FROM AuditLog a WHERE a.success = false AND a.timestamp > :since GROUP BY a.ipAddress HAVING COUNT(a) > :threshold")
    List<Object[]> findSuspiciousIpAddresses(@Param("since") LocalDateTime since, @Param("threshold") long threshold);
}