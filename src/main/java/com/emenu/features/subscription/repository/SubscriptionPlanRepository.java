package com.emenu.features.subscription.repository;

import com.emenu.enums.sub_scription.SubscriptionPlanStatus;
import com.emenu.features.subscription.models.SubscriptionPlan;
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
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    /**
     * Finds a non-deleted subscription plan by ID
     */
    Optional<SubscriptionPlan> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Checks if a non-deleted subscription plan exists with the given name
     */
    boolean existsByNameAndIsDeletedFalse(String name);

    /**
     * Searches subscription plans with filters for statuses and text search across name and description
     */
    @Query("""
        SELECT sp FROM SubscriptionPlan sp
        WHERE sp.isDeleted = false
        AND (:statuses IS NULL OR sp.status IN :statuses)
        AND (:search IS NULL OR :search = '' OR
             LOWER(sp.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(sp.description) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<SubscriptionPlan> findAllWithFilters(
            @Param("statuses") List<SubscriptionPlanStatus> statuses,
            @Param("search") String search,
            Pageable pageable
    );
}