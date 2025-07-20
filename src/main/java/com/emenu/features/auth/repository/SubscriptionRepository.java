package com.emenu.features.auth.repository;

import com.emenu.enums.SubscriptionPlan;
import com.emenu.features.auth.models.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID>, JpaSpecificationExecutor<Subscription> {
    
    Optional<Subscription> findByIdAndIsDeletedFalse(UUID id);
    
    List<Subscription> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    Page<Subscription> findByBusinessIdAndIsDeletedFalse(UUID businessId, Pageable pageable);
    
    @Query("SELECT s FROM Subscription s WHERE s.businessId = :businessId AND s.isActive = true AND s.isDeleted = false")
    Optional<Subscription> findActiveByBusinessId(@Param("businessId") UUID businessId);
    
    @Query("SELECT s FROM Subscription s WHERE s.businessId = :businessId AND s.isActive = true AND s.endDate > :now AND s.isDeleted = false")
    Optional<Subscription> findCurrentActiveByBusinessId(@Param("businessId") UUID businessId, @Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate < :now AND s.isActive = true AND s.isDeleted = false")
    List<Subscription> findExpiredSubscriptions(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate BETWEEN :start AND :end AND s.isActive = true AND s.isDeleted = false")
    List<Subscription> findExpiringSubscriptions(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.plan = :plan AND s.isDeleted = false")
    long countByPlan(@Param("plan") SubscriptionPlan plan);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.isActive = true AND s.isDeleted = false")
    long countActiveSubscriptions();
}