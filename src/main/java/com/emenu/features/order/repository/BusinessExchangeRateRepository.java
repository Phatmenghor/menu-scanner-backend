package com.emenu.features.order.repository;

import com.emenu.features.order.models.BusinessExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessExchangeRateRepository extends JpaRepository<BusinessExchangeRate, UUID>, JpaSpecificationExecutor<BusinessExchangeRate> {
    
    // Basic CRUD operations
    Optional<BusinessExchangeRate> findByIdAndIsDeletedFalse(UUID id);
    
    // Get active rate for a business
    @Query("SELECT ber FROM BusinessExchangeRate ber WHERE ber.businessId = :businessId AND ber.isActive = true AND ber.isDeleted = false")
    Optional<BusinessExchangeRate> findActiveRateByBusinessId(@Param("businessId") UUID businessId);
    
    // Get all rates for a business
    @Query("SELECT ber FROM BusinessExchangeRate ber WHERE ber.businessId = :businessId AND ber.isDeleted = false ORDER BY ber.createdAt DESC")
    List<BusinessExchangeRate> findAllByBusinessId(@Param("businessId") UUID businessId);
    
    // Check if business has any exchange rate
    @Query("SELECT COUNT(ber) > 0 FROM BusinessExchangeRate ber WHERE ber.businessId = :businessId AND ber.isDeleted = false")
    boolean existsByBusinessId(@Param("businessId") UUID businessId);
    
    // Check if business has active rate
    @Query("SELECT COUNT(ber) > 0 FROM BusinessExchangeRate ber WHERE ber.businessId = :businessId AND ber.isActive = true AND ber.isDeleted = false")
    boolean hasActiveRate(@Param("businessId") UUID businessId);
    
    // Deactivate all rates for a business (used when setting a new active rate)
    @Modifying
    @Query("UPDATE BusinessExchangeRate ber SET ber.isActive = false WHERE ber.businessId = :businessId AND ber.isActive = true AND ber.isDeleted = false")
    int deactivateAllRatesForBusiness(@Param("businessId") UUID businessId);
    
    // Count active rates for a business
    @Query("SELECT COUNT(ber) FROM BusinessExchangeRate ber WHERE ber.businessId = :businessId AND ber.isActive = true AND ber.isDeleted = false")
    long countActiveRates(@Param("businessId") UUID businessId);
    
    // Get all active rates across all businesses
    @Query("SELECT ber FROM BusinessExchangeRate ber WHERE ber.isActive = true AND ber.isDeleted = false ORDER BY ber.createdAt DESC")
    List<BusinessExchangeRate> findAllActiveRates();
}