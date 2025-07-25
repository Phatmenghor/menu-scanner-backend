package com.emenu.features.payment.repository;

import com.emenu.features.payment.models.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID>, JpaSpecificationExecutor<ExchangeRate> {
    
    // Basic CRUD operations
    Optional<ExchangeRate> findByIdAndIsDeletedFalse(UUID id);
    
    // Get current active rate (only one should be active at a time)
    @Query("SELECT er FROM ExchangeRate er WHERE er.isActive = true AND er.isDeleted = false")
    Optional<ExchangeRate> findActiveRate();
    
    // Count active rates
    @Query("SELECT COUNT(er) FROM ExchangeRate er WHERE er.isActive = true AND er.isDeleted = false")
    long countActiveRates();
}