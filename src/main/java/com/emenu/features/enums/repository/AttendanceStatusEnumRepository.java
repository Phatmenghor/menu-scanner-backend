package com.emenu.features.enums.repository;

import com.emenu.features.enums.models.AttendanceStatusEnum;
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
public interface AttendanceStatusEnumRepository extends JpaRepository<AttendanceStatusEnum, UUID> {
    
    Optional<AttendanceStatusEnum> findByIdAndIsDeletedFalse(UUID id);
    List<AttendanceStatusEnum> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    Optional<AttendanceStatusEnum> findByBusinessIdAndEnumNameAndIsDeletedFalse(
            UUID businessId, String enumName);
    
    @Query("SELECT e FROM AttendanceStatusEnum e WHERE e.isDeleted = false " +
           "AND (:businessId IS NULL OR e.businessId = :businessId) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(e.enumName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AttendanceStatusEnum> findWithFilters(
        @Param("businessId") UUID businessId,
        @Param("search") String search,
        Pageable pageable
    );
}