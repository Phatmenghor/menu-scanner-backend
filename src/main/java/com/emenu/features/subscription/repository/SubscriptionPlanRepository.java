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
    
    boolean existsByNameAndIsDeletedFalse(String name);
}