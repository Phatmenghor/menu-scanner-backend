package com.emenu.features.hr.repository;

import com.emenu.features.hr.models.WorkSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {

    /**
     * Finds a non-deleted work schedule by ID
     */
    Optional<WorkSchedule> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds all non-deleted work schedules for a given user
     */
    List<WorkSchedule> findByUserIdAndIsDeletedFalse(UUID userId);

    /**
     * Searches work schedules with filters for business, user, and text search
     */
    @Query("SELECT w FROM WorkSchedule w WHERE w.isDeleted = false " +
           "AND (:businessId IS NULL OR w.businessId = :businessId) " +
           "AND (:userId IS NULL OR w.userId = :userId) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<WorkSchedule> findWithFilters(
        @Param("businessId") UUID businessId,
        @Param("userId") UUID userId,
        @Param("search") String search,
        Pageable pageable
    );
}
