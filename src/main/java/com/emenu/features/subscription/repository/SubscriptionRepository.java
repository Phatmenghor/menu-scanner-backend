package com.emenu.features.subscription.repository;

import com.emenu.enums.SubscriptionStatus;
import com.emenu.features.subscription.models.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    
    Optional<Subscription> findByIdAndIsDeletedFalse(UUID id);
    Optional<Subscription> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    List<Subscription> findByStatusAndIsDeletedFalse(SubscriptionStatus status);
    Page<Subscription> findByIsDeletedFalse(Pageable pageable);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate < :now AND s.status = 'ACTIVE' AND s.isDeleted = false")
    List<Subscription> findExpiredSubscriptions(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate BETWEEN :start AND :end AND s.status = 'ACTIVE' AND s.isDeleted = false")
    List<Subscription> findExpiringSubscriptions(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.plan = :plan AND s.isDeleted = false")
    long countByPlan(@Param("plan") com.emenu.enums.SubscriptionPlan plan);
}