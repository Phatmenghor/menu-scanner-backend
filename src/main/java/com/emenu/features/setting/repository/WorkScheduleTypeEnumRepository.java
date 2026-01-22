package com.emenu.features.setting.repository;

import com.emenu.features.setting.models.WorkScheduleTypeEnum;
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
public interface WorkScheduleTypeEnumRepository extends JpaRepository<WorkScheduleTypeEnum, UUID> {

    /**
     * Finds a non-deleted work schedule type enum by ID
     */
    Optional<WorkScheduleTypeEnum> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds all non-deleted work schedule type enums for a business
     */
    List<WorkScheduleTypeEnum> findByBusinessIdAndIsDeletedFalse(UUID businessId);

    /**
     * Finds a non-deleted work schedule type enum by business ID and enum name
     */
    Optional<WorkScheduleTypeEnum> findByBusinessIdAndEnumNameAndIsDeletedFalse(
        UUID businessId, String enumName);

    /**
     * Searches work schedule type enums with filters for business and text search on enum name
     */
    @Query("SELECT e FROM WorkScheduleTypeEnum e WHERE e.isDeleted = false " +
           "AND (:businessId IS NULL OR e.businessId = :businessId) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(e.enumName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<WorkScheduleTypeEnum> findWithFilters(
        @Param("businessId") UUID businessId,
        @Param("search") String search,
        Pageable pageable
    );
}