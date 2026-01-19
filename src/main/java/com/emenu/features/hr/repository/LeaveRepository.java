package com.emenu.features.hr.repository;

import com.emenu.features.hr.models.Leave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, UUID> {
    
    Optional<Leave> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT l FROM Leave l WHERE l.isDeleted = false " +
           "AND (:businessId IS NULL OR l.businessId = :businessId) " +
           "AND (:userId IS NULL OR l.userId = :userId) " +
           "AND (:leaveTypeEnumId IS NULL OR l.leaveTypeEnumId = :leaveTypeEnumId) " +
           "AND (:startDate IS NULL OR l.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR l.endDate <= :endDate) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(l.reason) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Leave> findWithFilters(
        @Param("businessId") UUID businessId,
        @Param("userId") UUID userId,
        @Param("leaveTypeEnumId") UUID leaveTypeEnumId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("search") String search,
        Pageable pageable
    );
}