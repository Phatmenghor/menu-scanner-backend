package com.emenu.features.product.repository;

import com.emenu.features.product.models.ProductSize;
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
    
    Optional<ProductSize> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT ps FROM ProductSize ps " +
           "WHERE ps.productId = :productId AND ps.isDeleted = false " +
           "ORDER BY ps.price ASC")
    List<ProductSize> findByProductIdAndIsDeletedFalse(@Param("productId") UUID productId);

    /**
     * Batch load sizes for multiple products - ordered by price
     */
    @Query("SELECT ps FROM ProductSize ps " +
           "WHERE ps.productId IN :productIds AND ps.isDeleted = false " +
           "ORDER BY ps.productId, ps.price ASC")
    List<ProductSize> findByProductIdsAndIsDeletedFalse(@Param("productIds") List<UUID> productIds);

    /**
     * Group sizes by product ID for efficient batch processing
     */
    default Map<UUID, List<ProductSize>> findSizesByProductIdsGrouped(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        
        List<ProductSize> sizes = findByProductIdsAndIsDeletedFalse(productIds);
        return sizes.stream()
                .collect(Collectors.groupingBy(ProductSize::getProductId));
    }
}