package com.emenu.features.auth.repository;

import com.emenu.features.auth.models.ExchangeRate;
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
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID>, JpaSpecificationExecutor<ExchangeRate> {
    
    // Basic CRUD operations
    Optional<ExchangeRate> findByIdAndIsDeletedFalse(UUID id);
    
    Page<ExchangeRate> findByIsDeletedFalse(Pageable pageable);
    
    List<ExchangeRate> findByIsDeletedFalse();
    
    // System default operations
    @Query("SELECT er FROM ExchangeRate er WHERE er.isSystemDefault = true AND er.isActive = true AND er.isDeleted = false")
    Optional<ExchangeRate> findActiveSystemDefault();
    
    @Query("SELECT er FROM ExchangeRate er WHERE er.isSystemDefault = true AND er.isDeleted = false")
    List<ExchangeRate> findAllSystemDefaults();
    
    // Business specific operations
    @Query("SELECT er FROM ExchangeRate er WHERE er.businessId = :businessId AND er.isActive = true AND er.isDeleted = false")
    Optional<ExchangeRate> findActiveByBusinessId(@Param("businessId") UUID businessId);
    
    @Query("SELECT er FROM ExchangeRate er WHERE er.businessId = :businessId AND er.isDeleted = false ORDER BY er.createdAt DESC")
    List<ExchangeRate> findByBusinessIdAndIsDeletedFalse(@Param("businessId") UUID businessId);
    
    Page<ExchangeRate> findByBusinessIdAndIsDeletedFalse(UUID businessId, Pageable pageable);
    
    // Active rates
    @Query("SELECT er FROM ExchangeRate er WHERE er.isActive = true AND er.isDeleted = false")
    List<ExchangeRate> findAllActiveRates();
    
    Page<ExchangeRate> findByIsActiveAndIsDeletedFalse(Boolean isActive, Pageable pageable);
    
    // Check for existing rates
    @Query("SELECT COUNT(er) > 0 FROM ExchangeRate er WHERE er.businessId = :businessId AND er.isActive = true AND er.isDeleted = false")
    boolean hasActiveRateForBusiness(@Param("businessId") UUID businessId);
    
    @Query("SELECT COUNT(er) > 0 FROM ExchangeRate er WHERE er.isSystemDefault = true AND er.isActive = true AND er.isDeleted = false")
    boolean hasActiveSystemDefault();
    
    // Latest rates
    @Query("SELECT er FROM ExchangeRate er WHERE er.isDeleted = false ORDER BY er.createdAt DESC")
    Page<ExchangeRate> findLatestRates(Pageable pageable);
    
    // Business count
    @Query("SELECT COUNT(DISTINCT er.businessId) FROM ExchangeRate er WHERE er.businessId IS NOT NULL AND er.isDeleted = false")
    long countBusinessesWithRates();
}