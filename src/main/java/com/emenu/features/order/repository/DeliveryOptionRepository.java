package com.emenu.features.order.repository;

import com.emenu.enums.common.Status;
import com.emenu.features.order.models.DeliveryOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryOptionRepository extends JpaRepository<DeliveryOption, UUID>, JpaSpecificationExecutor<DeliveryOption> {

    /**
     * Finds all non-deleted delivery options by business ID, ordered by creation date descending
     */
    List<DeliveryOption> findByBusinessIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID businessId);

    /**
     * Finds non-deleted delivery options by business ID and status, ordered by name ascending
     */
    @Query("SELECT d FROM DeliveryOption d WHERE d.businessId = :businessId AND d.status = :status AND d.isDeleted = false ORDER BY d.name ASC")
    List<DeliveryOption> findActiveByBusinessId(@Param("businessId") UUID businessId, @Param("status") Status status);

    /**
     * Finds a non-deleted delivery option by ID
     */
    Optional<DeliveryOption> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Checks if a non-deleted delivery option exists with the given name and business ID
     */
    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);

    /**
     * Finds a non-deleted delivery option by ID with business details eagerly fetched
     */
    @Query("SELECT do FROM DeliveryOption do " +
            "LEFT JOIN FETCH do.business " +
            "WHERE do.id = :id AND do.isDeleted = false")
    Optional<DeliveryOption> findByIdWithBusiness(@Param("id") UUID id);
}