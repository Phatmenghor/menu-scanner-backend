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

    Optional<SubscriptionPlan> findByIdAndIsDeletedFalse(UUID id);

    boolean existsByNameAndIsDeletedFalse(String name);

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