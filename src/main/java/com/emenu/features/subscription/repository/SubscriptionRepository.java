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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID>, JpaSpecificationExecutor<Subscription> {
    Optional<Subscription> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT s FROM Subscription s WHERE s.businessId = :businessId AND s.isActive = true AND s.endDate > :now AND s.isDeleted = false")
    Optional<Subscription> findCurrentActiveByBusinessId(@Param("businessId") UUID businessId, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.planId = :planId AND s.isDeleted = false")
    long countByPlan(@Param("planId") UUID planId);
}
