package com.emenu.features.auth.repository;

import com.emenu.features.auth.models.ExchangeRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    Page<ExchangeRate> findByIsDeletedFalse(Pageable pageable);
    
    List<ExchangeRate> findByIsDeletedFalse();
    
    // Get current active rate (only one should be active at a time)
    @Query("SELECT er FROM ExchangeRate er WHERE er.isActive = true AND er.isDeleted = false")
    Optional<ExchangeRate> findActiveRate();
    
    // Get all rates ordered by creation date (for history)
    @Query("SELECT er FROM ExchangeRate er WHERE er.isDeleted = false ORDER BY er.createdAt DESC")
    List<ExchangeRate> findAllRatesHistory();
    
    // Active rates
    @Query("SELECT er FROM ExchangeRate er WHERE er.isActive = true AND er.isDeleted = false")
    List<ExchangeRate> findAllActiveRates();
    
    Page<ExchangeRate> findByIsActiveAndIsDeletedFalse(Boolean isActive, Pageable pageable);
    
    // Check if there's an active rate
    @Query("SELECT COUNT(er) > 0 FROM ExchangeRate er WHERE er.isActive = true AND er.isDeleted = false")
    boolean hasActiveRate();
    
    // Count total rates
    @Query("SELECT COUNT(er) FROM ExchangeRate er WHERE er.isDeleted = false")
    long countAllRates();
    
    // Count active rates
    @Query("SELECT COUNT(er) FROM ExchangeRate er WHERE er.isActive = true AND er.isDeleted = false")
    long countActiveRates();
}