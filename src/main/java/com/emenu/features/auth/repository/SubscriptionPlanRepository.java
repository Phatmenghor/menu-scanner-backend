package com.emenu.features.auth.repository;

import com.emenu.features.auth.models.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID>, JpaSpecificationExecutor<SubscriptionPlan> {
    
    Optional<SubscriptionPlan> findByIdAndIsDeletedFalse(UUID id);
    Optional<SubscriptionPlan> findByNameAndIsDeletedFalse(String name);
    List<SubscriptionPlan> findByIsActiveAndIsDeletedFalseOrderBySortOrder(Boolean isActive);
    List<SubscriptionPlan> findByIsDefaultAndIsDeletedFalse(Boolean isDefault);
    Page<SubscriptionPlan> findByIsDeletedFalse(Pageable pageable);
    
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.isActive = true AND sp.isDeleted = false AND sp.isCustom = false ORDER BY sp.sortOrder")
    List<SubscriptionPlan> findPublicPlans();
    
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.isActive = true AND sp.isDeleted = false ORDER BY sp.sortOrder")
    List<SubscriptionPlan> findAllActivePlans();
    
    boolean existsByNameAndIsDeletedFalse(String name);
}