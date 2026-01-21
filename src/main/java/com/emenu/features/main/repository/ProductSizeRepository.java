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
    
    Optional<ProductSize> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT ps FROM ProductSize ps " +
           "WHERE ps.productId = :productId AND ps.isDeleted = false " +
           "ORDER BY ps.price ASC")
    List<ProductSize> findByProductId(@Param("productId") UUID productId);

    @Query("SELECT ps FROM ProductSize ps " +
           "WHERE ps.productId IN :productIds AND ps.isDeleted = false " +
           "ORDER BY ps.productId, ps.price ASC")
    List<ProductSize> findByProductIds(@Param("productIds") List<UUID> productIds);

    default Map<UUID, List<ProductSize>> findSizesByProductIdsGrouped(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        
        List<ProductSize> sizes = findByProductIds(productIds);
        return sizes.stream()
                .collect(Collectors.groupingBy(ProductSize::getProductId));
    }
}