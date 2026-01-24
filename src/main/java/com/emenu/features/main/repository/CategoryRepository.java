package com.emenu.features.main.repository;

import com.emenu.enums.common.Status;
import com.emenu.features.main.models.Category;
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
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Finds a non-deleted category by ID
     */
    Optional<Category> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds a non-deleted category by ID with business details eagerly fetched
     */
    @Query("SELECT c FROM Category c " +
           "LEFT JOIN FETCH c.business " +
           "WHERE c.id = :id AND c.isDeleted = false")
    Optional<Category> findByIdWithBusiness(@Param("id") UUID id);

    /**
     * Checks if a non-deleted category exists with the given name and business ID
     */
    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);

    /**
     * Find all categories with dynamic filtering - paginated
     */
    @Query("SELECT DISTINCT c FROM Category c " +
           "LEFT JOIN c.business b " +
           "WHERE c.isDeleted = false " +
           "AND (:businessId IS NULL OR c.businessId = :businessId) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Category> findAllWithFilters(
        @Param("businessId") UUID businessId,
        @Param("status") Status status,
        @Param("search") String search,
        Pageable pageable
    );

    /**
     * Find all categories with dynamic filtering - non-paginated
     */
    @Query("SELECT DISTINCT c FROM Category c " +
           "LEFT JOIN c.business b " +
           "WHERE c.isDeleted = false " +
           "AND (:businessId IS NULL OR c.businessId = :businessId) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Category> findAllWithFilters(
        @Param("businessId") UUID businessId,
        @Param("status") Status status,
        @Param("search") String search,
        Sort sort
    );
}
