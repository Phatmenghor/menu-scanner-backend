package com.emenu.features.business.repository;

import com.emenu.features.business.models.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BannerRepository extends JpaRepository<Banner, UUID>, JpaSpecificationExecutor<Banner> {
    
    Optional<Banner> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT b FROM Banner b WHERE b.businessId = :businessId AND b.isDeleted = false ORDER BY b.displayOrder ASC")
    List<Banner> findByBusinessIdOrderByDisplayOrder(@Param("businessId") UUID businessId);
    
    @Query("SELECT b FROM Banner b WHERE b.businessId = :businessId AND b.isActive = true AND b.isDeleted = false ORDER BY b.displayOrder ASC")
    List<Banner> findActiveByBusinessId(@Param("businessId") UUID businessId);
    
    @Query("SELECT b FROM Banner b " +
           "LEFT JOIN FETCH b.business " +
           "WHERE b.id = :id AND b.isDeleted = false")
    Optional<Banner> findByIdWithBusiness(@Param("id") UUID id);
    
    @Query("SELECT COUNT(b) FROM Banner b WHERE b.businessId = :businessId AND b.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);
}