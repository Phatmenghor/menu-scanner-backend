package com.emenu.features.hr.repository;

import com.emenu.features.hr.models.Attendance;
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
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    
    Optional<Attendance> findByIdAndIsDeletedFalse(UUID id);
    
    Optional<Attendance> findByUserIdAndAttendanceDateAndIsDeletedFalse(
        UUID userId, LocalDate date);
    
    @Query("SELECT a FROM Attendance a WHERE a.isDeleted = false " +
           "AND (:businessId IS NULL OR a.businessId = :businessId) " +
           "AND (:userId IS NULL OR a.userId = :userId) " +
           "AND (:startDate IS NULL OR a.attendanceDate >= :startDate) " +
           "AND (:endDate IS NULL OR a.attendanceDate <= :endDate) " +
           "AND (:statusEnumId IS NULL OR a.statusEnumId = :statusEnumId) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(a.remarks) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Attendance> findWithFilters(
        @Param("businessId") UUID businessId,
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("statusEnumId") UUID statusEnumId,
        @Param("search") String search,
        Pageable pageable
    );
}