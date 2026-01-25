package com.emenu.features.order.repository;

import com.emenu.enums.common.Status;
import com.emenu.features.order.models.DeliveryOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryOptionRepository extends JpaRepository<DeliveryOption, UUID> {

    /**
     * Finds all non-deleted delivery options by business ID, ordered by creation date descending
     */
    List<DeliveryOption> findByBusinessIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID businessId);

    /**
     * Finds non-deleted delivery options by business ID and status, ordered by name ascending
     */
    @Query("SELECT d FROM DeliveryOption d WHERE d.businessId = :businessId AND d.status = :status AND d.isDeleted = false ORDER BY d.name ASC")
    List<DeliveryOption> findActiveByBusinessId(@Param("businessId") UUID businessId, @Param("status") Status status);

    /**
     * Finds a non-deleted delivery option by ID
     */
    Optional<DeliveryOption> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Checks if a non-deleted delivery option exists with the given name and business ID
     */
    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);

    /**
     * Finds a non-deleted delivery option by ID with business details eagerly fetched
     */
    @Query("SELECT do FROM DeliveryOption do " +
            "LEFT JOIN FETCH do.business " +
            "WHERE do.id = :id AND do.isDeleted = false")
    Optional<DeliveryOption> findByIdWithBusiness(@Param("id") UUID id);

    /**
     * Find all delivery options with dynamic filtering - paginated
     */
    @Query("SELECT DISTINCT d FROM DeliveryOption d " +
           "LEFT JOIN d.business b " +
           "WHERE d.isDeleted = false " +
           "AND (:businessId IS NULL OR d.businessId = :businessId) " +
           "AND (:statuses IS NULL OR d.status IN :statuses) " +
           "AND (:minPrice IS NULL OR d.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR d.price <= :maxPrice) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DeliveryOption> findAllWithFilters(
        @Param("businessId") UUID businessId,
        @Param("statuses") List<Status> statuses,
        @Param("search") String search,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );

    /**
     * Find all delivery options with dynamic filtering - non-paginated
     */
    @Query("SELECT DISTINCT d FROM DeliveryOption d " +
           "LEFT JOIN d.business b " +
           "WHERE d.isDeleted = false " +
           "AND (:businessId IS NULL OR d.businessId = :businessId) " +
           "AND (:statuses IS NULL OR d.status IN :statuses) " +
           "AND (:minPrice IS NULL OR d.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR d.price <= :maxPrice) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<DeliveryOption> findAllWithFilters(
        @Param("businessId") UUID businessId,
        @Param("statuses") List<Status> statuses,
        @Param("search") String search,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Sort sort
    );
}