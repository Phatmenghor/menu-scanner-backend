package com.emenu.features.main.repository;

import com.emenu.features.main.models.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, UUID> {

    /**
     * Finds a non-deleted product size by ID
     */
    Optional<ProductSize> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds all non-deleted product sizes by product ID, ordered by price ascending
     */
    @Query("SELECT ps FROM ProductSize ps " +
           "WHERE ps.productId = :productId AND ps.isDeleted = false " +
           "ORDER BY ps.price ASC")
    List<ProductSize> findByProductId(@Param("productId") UUID productId);

    /**
     * Finds all non-deleted product sizes for multiple product IDs, ordered by product ID and price
     */
    @Query("SELECT ps FROM ProductSize ps " +
           "WHERE ps.productId IN :productIds AND ps.isDeleted = false " +
           "ORDER BY ps.productId, ps.price ASC")
    List<ProductSize> findByProductIds(@Param("productIds") List<UUID> productIds);

    /**
     * Finds and groups product sizes by product ID for multiple products
     */
    default Map<UUID, List<ProductSize>> findSizesByProductIdsGrouped(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        List<ProductSize> sizes = findByProductIds(productIds);
        return sizes.stream()
                .collect(Collectors.groupingBy(ProductSize::getProductId));
    }
}