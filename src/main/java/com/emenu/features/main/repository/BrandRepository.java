package com.emenu.features.main.repository;

import com.emenu.features.main.models.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID>, JpaSpecificationExecutor<Brand> {

    /**
     * Finds a non-deleted brand by ID
     */
    Optional<Brand> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds a non-deleted brand by ID with business details eagerly fetched
     */
    @Query("SELECT b FROM Brand b " +
           "LEFT JOIN FETCH b.business " +
           "WHERE b.id = :id AND b.isDeleted = false")
    Optional<Brand> findByIdWithBusiness(@Param("id") UUID id);

    /**
     * Counts non-deleted brands by business ID
     */
    @Query("SELECT COUNT(b) FROM Brand b WHERE b.businessId = :businessId AND b.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);

    /**
     * Checks if a non-deleted brand exists with the given name and business ID
     */
    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);
}