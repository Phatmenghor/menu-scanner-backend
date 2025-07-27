package com.emenu.features.business.repository;

import com.emenu.enums.common.Status;
import com.emenu.features.business.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {
    
    Optional<Category> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT c FROM Category c WHERE c.businessId = :businessId AND c.isDeleted = false ORDER BY c.name ASC")
    List<Category> findByBusinessIdOrderByName(@Param("businessId") UUID businessId);
    
    @Query("SELECT c FROM Category c WHERE c.businessId = :businessId AND c.status = :status AND c.isDeleted = false ORDER BY c.name ASC")
    List<Category> findActiveByBusinessId(@Param("businessId") UUID businessId, @Param("status") Status status);
    
    @Query("SELECT c FROM Category c " +
           "LEFT JOIN FETCH c.business " +
           "WHERE c.id = :id AND c.isDeleted = false")
    Optional<Category> findByIdWithBusiness(@Param("id") UUID id);
    
    @Query("SELECT COUNT(c) FROM Category c WHERE c.businessId = :businessId AND c.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);
    
    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);
}
