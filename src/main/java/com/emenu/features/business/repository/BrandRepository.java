package com.emenu.features.business.repository;

import com.emenu.features.business.models.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID>, JpaSpecificationExecutor<Brand> {
    
    Optional<Brand> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT b FROM Brand b " +
           "LEFT JOIN FETCH b.business " +
           "WHERE b.id = :id AND b.isDeleted = false")
    Optional<Brand> findByIdWithBusiness(@Param("id") UUID id);
    
    @Query("SELECT COUNT(b) FROM Brand b WHERE b.businessId = :businessId AND b.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);
    
    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);
}