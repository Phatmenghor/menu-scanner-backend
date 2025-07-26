package com.emenu.features.subscription.repository;

import com.emenu.features.subscription.models.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID>, JpaSpecificationExecutor<Subscription> {
    
    // ✅ FIXED: Load relationships properly
    @Query("SELECT s FROM Subscription s " +
           "LEFT JOIN FETCH s.business " +
           "LEFT JOIN FETCH s.plan " +
           "WHERE s.id = :id AND s.isDeleted = false")
    Optional<Subscription> findByIdAndIsDeletedFalse(@Param("id") UUID id);
    
    // ✅ FIXED: Load relationships for current active subscription
    @Query("SELECT s FROM Subscription s " +
           "LEFT JOIN FETCH s.business " +
           "LEFT JOIN FETCH s.plan " +
           "WHERE s.businessId = :businessId " +
           "AND s.isActive = true " +
           "AND s.endDate > :now " +
           "AND s.isDeleted = false")
    Optional<Subscription> findCurrentActiveByBusinessId(@Param("businessId") UUID businessId, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.planId = :planId AND s.isDeleted = false")
    long countByPlan(@Param("planId") UUID planId);
    
    // ✅ NEW: Find all subscriptions with relationships for pagination
    @Query(value = "SELECT s FROM Subscription s " +
                   "LEFT JOIN FETCH s.business " +
                   "LEFT JOIN FETCH s.plan " +
                   "WHERE s.isDeleted = false",
           countQuery = "SELECT COUNT(s) FROM Subscription s WHERE s.isDeleted = false")
    Page<Subscription> findAllWithRelationships(Pageable pageable);
    
    // ✅ NEW: Find business subscriptions with relationships
    @Query(value = "SELECT s FROM Subscription s " +
                   "LEFT JOIN FETCH s.business " +
                   "LEFT JOIN FETCH s.plan " +
                   "WHERE s.businessId = :businessId AND s.isDeleted = false",
           countQuery = "SELECT COUNT(s) FROM Subscription s WHERE s.businessId = :businessId AND s.isDeleted = false")
    Page<Subscription> findByBusinessIdWithRelationships(@Param("businessId") UUID businessId, Pageable pageable);
}