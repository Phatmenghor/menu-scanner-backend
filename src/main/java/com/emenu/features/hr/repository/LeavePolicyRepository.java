package com.emenu.features.hr.repository;

import com.emenu.features.hr.models.LeavePolicy;
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
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, UUID> {
    
    Optional<LeavePolicy> findByIdAndIsDeletedFalse(UUID id);
    List<LeavePolicy> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    @Query("SELECT p FROM LeavePolicy p WHERE p.isDeleted = false " +
           "AND (:businessId IS NULL OR p.businessId = :businessId) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(p.policyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<LeavePolicy> findWithFilters(
        @Param("businessId") UUID businessId,
        @Param("search") String search,
        Pageable pageable
    );
}