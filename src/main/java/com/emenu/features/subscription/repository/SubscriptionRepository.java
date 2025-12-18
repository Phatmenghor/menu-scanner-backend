package com.emenu.features.subscription.repository;

import com.emenu.features.subscription.models.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.business b LEFT JOIN FETCH s.plan p WHERE s.id = :id AND s.isDeleted = false")
    Optional<Subscription> findByIdAndIsDeletedFalse(@Param("id") UUID id);

    @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.business b LEFT JOIN FETCH s.plan p WHERE s.businessId = :businessId AND s.endDate > :now AND s.isDeleted = false ORDER BY s.endDate DESC")
    Optional<Subscription> findCurrentActiveByBusinessId(@Param("businessId") UUID businessId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.planId = :planId AND s.isDeleted = false")
    long countByPlan(@Param("planId") UUID planId);

    @Query("""
                SELECT s FROM Subscription s
                LEFT JOIN s.business b
                LEFT JOIN s.plan p
                WHERE s.isDeleted = false
                AND (:businessId IS NULL OR s.businessId = :businessId)
                AND (:planId IS NULL OR s.planId = :planId)
                AND (:autoRenew IS NULL OR s.autoRenew = :autoRenew)
                AND (:startDate IS NULL OR CAST(s.startDate AS date) >= :startDate)
                AND (:toDate IS NULL OR CAST(s.startDate AS date) <= :toDate)
                AND (
                    :status IS NULL
                    OR (:status = 'ACTIVE' AND s.endDate > :now)
                    OR (:status = 'EXPIRED' AND s.endDate <= :now)
                    OR (
                        :status = 'EXPIRING_SOON'
                        AND s.endDate > :now
                        AND s.endDate <= :expiryThreshold
                    )
                )
                AND (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
                )
            """)
    Page<Subscription> findWithFilters(
            @Param("businessId") UUID businessId,
            @Param("planId") UUID planId,
            @Param("autoRenew") Boolean autoRenew,
            @Param("startDate") LocalDate startDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status,
            @Param("now") LocalDateTime now,
            @Param("expiryThreshold") LocalDateTime expiryThreshold,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.business b LEFT JOIN FETCH s.plan p WHERE s.id = :id")
    Optional<Subscription> findByIdWithRelationships(@Param("id") UUID id);

    /**
     * Find all subscriptions by business ID
     */
    @Query("""
                SELECT s FROM Subscription s
                LEFT JOIN FETCH s.plan
                WHERE s.businessId = :businessId 
                AND s.isDeleted = false
                ORDER BY s.createdAt DESC
            """)
    List<Subscription> findByBusinessIdAndIsDeletedFalse(@Param("businessId") UUID businessId);
}