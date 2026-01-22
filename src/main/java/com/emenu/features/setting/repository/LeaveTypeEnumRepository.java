package com.emenu.features.setting.repository;

import com.emenu.features.setting.models.LeaveTypeEnum;
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
public interface LeaveTypeEnumRepository extends JpaRepository<LeaveTypeEnum, UUID> {

    /**
     * Finds a non-deleted leave type enum by ID
     */
    Optional<LeaveTypeEnum> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds all non-deleted leave type enums for a business
     */
    List<LeaveTypeEnum> findByBusinessIdAndIsDeletedFalse(UUID businessId);

    /**
     * Finds a non-deleted leave type enum by business ID and enum name
     */
    Optional<LeaveTypeEnum> findByBusinessIdAndEnumNameAndIsDeletedFalse(
        UUID businessId, String enumName);

    /**
     * Searches leave type enums with filters for business and text search on enum name
     */
    @Query("SELECT e FROM LeaveTypeEnum e WHERE e.isDeleted = false " +
           "AND (:businessId IS NULL OR e.businessId = :businessId) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(e.enumName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<LeaveTypeEnum> findWithFilters(
        @Param("businessId") UUID businessId,
        @Param("search") String search,
        Pageable pageable
    );
}