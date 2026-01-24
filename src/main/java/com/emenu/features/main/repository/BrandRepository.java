package com.emenu.features.main.repository;

import com.emenu.enums.common.Status;
import com.emenu.features.main.models.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {

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

    /**
     * Find all brands with dynamic filtering - paginated
     */
    @Query("SELECT DISTINCT b FROM Brand b " +
           "LEFT JOIN b.business bus " +
           "WHERE b.isDeleted = false " +
           "AND (:businessId IS NULL OR b.businessId = :businessId) " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(b.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(bus.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Brand> findAllWithFilters(
        @Param("businessId") UUID businessId,
        @Param("status") Status status,
        @Param("search") String search,
        Pageable pageable
    );

    /**
     * Find all brands with dynamic filtering - non-paginated
     */
    @Query("SELECT DISTINCT b FROM Brand b " +
           "LEFT JOIN b.business bus " +
           "WHERE b.isDeleted = false " +
           "AND (:businessId IS NULL OR b.businessId = :businessId) " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(b.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(bus.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Brand> findAllWithFilters(
        @Param("businessId") UUID businessId,
        @Param("status") Status status,
        @Param("search") String search,
        Sort sort
    );
}