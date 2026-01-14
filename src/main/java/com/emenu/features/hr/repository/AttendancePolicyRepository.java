package com.emenu.features.hr.repository;

import com.emenu.features.hr.models.AttendancePolicy;
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
public interface AttendancePolicyRepository extends JpaRepository<AttendancePolicy, UUID> {
    
    Optional<AttendancePolicy> findByIdAndIsDeletedFalse(UUID id);
    List<AttendancePolicy> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    @Query("SELECT p FROM AttendancePolicy p WHERE p.isDeleted = false " +
           "AND (:businessId IS NULL OR p.businessId = :businessId) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(p.policyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AttendancePolicy> findWithFilters(
        @Param("businessId") UUID businessId,
        @Param("search") String search,
        Pageable pageable
    );
}