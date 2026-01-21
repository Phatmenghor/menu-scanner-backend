package com.emenu.features.main.repository;

import com.emenu.features.main.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {
    
    Optional<Category> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT c FROM Category c " +
           "LEFT JOIN FETCH c.business " +
           "WHERE c.id = :id AND c.isDeleted = false")
    Optional<Category> findByIdWithBusiness(@Param("id") UUID id);

    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);
}
