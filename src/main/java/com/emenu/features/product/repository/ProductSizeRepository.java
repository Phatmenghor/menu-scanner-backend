package com.emenu.features.product.repository;

import com.emenu.features.product.models.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    // ✅ OPTIMIZED: Batch load sizes for multiple products in one query
    @Query("SELECT ps FROM ProductSize ps " +
           "WHERE ps.productId IN :productIds AND ps.isDeleted = false " +
           "ORDER BY ps.productId, ps.price ASC")
    List<ProductSize> findByProductIdsAndIsDeletedFalse(@Param("productIds") List<UUID> productIds);

    // ✅ NEW: Batch load sizes with counts for performance monitoring
    @Query("SELECT ps.productId, COUNT(ps) FROM ProductSize ps " +
           "WHERE ps.productId IN :productIds AND ps.isDeleted = false " +
           "GROUP BY ps.productId")
    List<Object[]> countSizesByProductIds(@Param("productIds") List<UUID> productIds);

    // ✅ NEW: Get products that have sizes (for filtering)
    @Query("SELECT DISTINCT ps.productId FROM ProductSize ps " +
           "WHERE ps.productId IN :productIds AND ps.isDeleted = false")
    List<UUID> findProductIdsWithSizes(@Param("productIds") List<UUID> productIds);

    @Modifying
    @Query("UPDATE ProductSize ps SET ps.promotionType = NULL, ps.promotionValue = NULL, " +
            "ps.promotionFromDate = NULL, ps.promotionToDate = NULL " +
            "WHERE ps.promotionToDate < :now AND ps.promotionToDate IS NOT NULL")
    int clearExpiredPromotions(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE ProductSize ps SET ps.promotionType = NULL, ps.promotionValue = NULL, " +
            "ps.promotionFromDate = NULL, ps.promotionToDate = NULL " +
            "WHERE ps.product.businessId = :businessId AND ps.isDeleted = false")
    int clearAllPromotionsForBusiness(@Param("businessId") UUID businessId);

    // ✅ HELPER: Default method to group sizes by product ID
    default Map<UUID, List<ProductSize>> findSizesByProductIdsGrouped(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        
        List<ProductSize> sizes = findByProductIdsAndIsDeletedFalse(productIds);
        return sizes.stream()
                .collect(Collectors.groupingBy(ProductSize::getProductId));
    }
}