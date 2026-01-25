package com.emenu.features.order.repository;

import com.emenu.features.order.models.ExchangeRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {

    /**
     * Finds a non-deleted exchange rate by ID
     */
    Optional<ExchangeRate> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds the current active exchange rate (only one should be active at a time)
     */
    @Query("SELECT er FROM ExchangeRate er WHERE er.isActive = true AND er.isDeleted = false")
    Optional<ExchangeRate> findActiveRate();

    /**
     * Counts active exchange rates
     */
    @Query("SELECT COUNT(er) FROM ExchangeRate er WHERE er.isActive = true AND er.isDeleted = false")
    long countActiveRates();

    /**
     * Find all exchange rates with dynamic filtering
     */
    @Query("SELECT er FROM ExchangeRate er " +
           "WHERE er.isDeleted = false " +
           "AND (:isActive IS NULL OR er.isActive = :isActive) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     CAST(er.usdToKhrRate AS string) LIKE CONCAT('%', :search, '%'))")
    Page<ExchangeRate> findAllWithFilters(
        @Param("isActive") Boolean isActive,
        @Param("search") String search,
        Pageable pageable
    );
}