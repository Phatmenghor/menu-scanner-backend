package com.emenu.features.order.repository;

import com.emenu.features.order.models.BusinessExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessExchangeRateRepository extends JpaRepository<BusinessExchangeRate, UUID> {

    /**
     * Finds a non-deleted business exchange rate by ID
     */
    Optional<BusinessExchangeRate> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds the active exchange rate for a business
     */
    @Query("SELECT ber FROM BusinessExchangeRate ber WHERE ber.businessId = :businessId AND ber.isActive = true AND ber.isDeleted = false")
    Optional<BusinessExchangeRate> findActiveRateByBusinessId(@Param("businessId") UUID businessId);

    /**
     * Finds all non-deleted exchange rates for a business, ordered by creation date descending
     */
    @Query("SELECT ber FROM BusinessExchangeRate ber WHERE ber.businessId = :businessId AND ber.isDeleted = false ORDER BY ber.createdAt DESC")
    List<BusinessExchangeRate> findAllByBusinessId(@Param("businessId") UUID businessId);

    /**
     * Checks if a business has any non-deleted exchange rate
     */
    @Query("SELECT COUNT(ber) > 0 FROM BusinessExchangeRate ber WHERE ber.businessId = :businessId AND ber.isDeleted = false")
    boolean existsByBusinessId(@Param("businessId") UUID businessId);

    /**
     * Checks if a business has an active exchange rate
     */
    @Query("SELECT COUNT(ber) > 0 FROM BusinessExchangeRate ber WHERE ber.businessId = :businessId AND ber.isActive = true AND ber.isDeleted = false")
    boolean hasActiveRate(@Param("businessId") UUID businessId);

    /**
     * Deactivates all active exchange rates for a business
     */
    @Modifying
    @Query("UPDATE BusinessExchangeRate ber SET ber.isActive = false WHERE ber.businessId = :businessId AND ber.isActive = true AND ber.isDeleted = false")
    int deactivateAllRatesForBusiness(@Param("businessId") UUID businessId);

    /**
     * Counts active exchange rates for a business
     */
    @Query("SELECT COUNT(ber) FROM BusinessExchangeRate ber WHERE ber.businessId = :businessId AND ber.isActive = true AND ber.isDeleted = false")
    long countActiveRates(@Param("businessId") UUID businessId);

    /**
     * Finds all active exchange rates across all businesses, ordered by creation date descending
     */
    @Query("SELECT ber FROM BusinessExchangeRate ber WHERE ber.isActive = true AND ber.isDeleted = false ORDER BY ber.createdAt DESC")
    List<BusinessExchangeRate> findAllActiveRates();
}