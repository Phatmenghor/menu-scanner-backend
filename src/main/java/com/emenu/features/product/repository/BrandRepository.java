package com.emenu.features.product.repository;

import com.emenu.enums.common.Status;
import com.emenu.features.product.models.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID>, JpaSpecificationExecutor<Brand> {
    
    Optional<Brand> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT b FROM Brand b WHERE b.businessId = :businessId AND b.status = :status AND b.isDeleted = false ORDER BY b.name ASC")
    List<Brand> findActiveByBusinessId(@Param("businessId") UUID businessId, @Param("status") Status status);
    
    @Query("SELECT b FROM Brand b " +
           "LEFT JOIN FETCH b.business " +
           "WHERE b.id = :id AND b.isDeleted = false")
    Optional<Brand> findByIdWithBusiness(@Param("id") UUID id);
    
    @Query("SELECT COUNT(b) FROM Brand b WHERE b.businessId = :businessId AND b.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);
    
    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);
}