package com.emenu.features.services.repository;

import com.emenu.enums.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    Optional<SubscriptionPlan> findByIdAndIsDeletedFalse(UUID id);
    boolean existsByNameAndIsDeletedFalse(String name);
    List<SubscriptionPlan> findByIsActiveTrueAndIsDeletedFalseOrderBySortOrder();
    List<SubscriptionPlan> findByIsDeletedFalseOrderBySortOrder();
}