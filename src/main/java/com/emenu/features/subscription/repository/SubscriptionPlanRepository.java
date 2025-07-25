package com.emenu.features.subscription.repository;

import com.emenu.enums.sub_scription.SubscriptionPlanStatus;
import com.emenu.features.subscription.models.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID>, JpaSpecificationExecutor<SubscriptionPlan> {
    
    Optional<SubscriptionPlan> findByIdAndIsDeletedFalse(UUID id);
    Optional<SubscriptionPlan> findByNameAndIsDeletedFalse(String name);
    Page<SubscriptionPlan> findByIsDeletedFalse(Pageable pageable);
    
    // ✅ UPDATED: Use status enum instead of isActive/isDefault fields
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.status = :status AND sp.isDeleted = false ORDER BY sp.price ASC")
    List<SubscriptionPlan> findByStatusAndIsDeletedFalse(@Param("status") SubscriptionPlanStatus status);
    
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.status = 'PUBLIC' AND sp.isDeleted = false ORDER BY sp.price ASC")
    List<SubscriptionPlan> findPublicPlans();
    
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.isDeleted = false ORDER BY sp.price ASC")
    List<SubscriptionPlan> findAllActivePlans();
    
    boolean existsByNameAndIsDeletedFalse(String name);

    // ✅ ADDED: Additional useful queries for the simplified structure
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.price = 0.0 AND sp.isDeleted = false")
    List<SubscriptionPlan> findFreePlans();

    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.status = 'PRIVATE' AND sp.isDeleted = false")
    List<SubscriptionPlan> findPrivatePlans();

    @Query("SELECT COUNT(sp) FROM SubscriptionPlan sp WHERE sp.status = :status AND sp.isDeleted = false")
    long countByStatusAndIsDeletedFalse(@Param("status") SubscriptionPlanStatus status);

    @Query("SELECT COUNT(sp) FROM SubscriptionPlan sp WHERE sp.price = 0.0 AND sp.isDeleted = false")
    long countFreePlans();
}