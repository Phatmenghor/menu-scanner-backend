package com.emenu.features.enums.repository;

import com.emenu.features.enums.models.WorkScheduleTypeEnum;
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
    
    Optional<WorkScheduleTypeEnum> findByIdAndIsDeletedFalse(UUID id);
    List<WorkScheduleTypeEnum> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    Optional<WorkScheduleTypeEnum> findByBusinessIdAndEnumNameAndIsDeletedFalse(
        UUID businessId, String enumName);
    
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