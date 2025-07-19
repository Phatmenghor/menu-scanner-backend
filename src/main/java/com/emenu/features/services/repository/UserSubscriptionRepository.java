package com.emenu.features.services.repository;

import com.emenu.enums.SubscriptionStatus;
import com.emenu.features.services.domain.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID>, JpaSpecificationExecutor<UserSubscription> {
    Optional<UserSubscription> findByIdAndIsDeletedFalse(UUID id);
    Optional<UserSubscription> findByUserIdAndStatusAndIsDeletedFalse(UUID userId, SubscriptionStatus status);
    boolean existsByUserIdAndStatusAndIsDeletedFalse(UUID userId, SubscriptionStatus status);
}