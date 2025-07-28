package com.emenu.features.product.repository;

import com.emenu.features.product.models.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, UUID> {
    
    @Query("SELECT ps FROM ProductSize ps WHERE ps.productId = :productId AND ps.isDeleted = false ORDER BY ps.sortOrder ASC, ps.finalPrice ASC")
    List<ProductSize> findByProductIdOrderBySortAndPrice(@Param("productId") UUID productId);
    
    @Query("SELECT ps FROM ProductSize ps WHERE ps.productId = :productId AND ps.isDefault = true AND ps.isDeleted = false")
    Optional<ProductSize> findDefaultByProductId(@Param("productId") UUID productId);
    
    @Query("SELECT ps FROM ProductSize ps WHERE ps.productId = :productId AND ps.hasPromotion = true AND ps.isDeleted = false ORDER BY ps.finalPrice ASC")
    List<ProductSize> findPromotionalSizesByProductId(@Param("productId") UUID productId);
    
    void deleteByProductIdAndIsDeletedFalse(UUID productId);

    @Modifying
    @Query("UPDATE ProductSize ps SET ps.promotionType = NULL, ps.promotionValue = NULL, " +
            "ps.promotionFromDate = NULL, ps.promotionToDate = NULL " +
            "WHERE ps.promotionToDate < :now AND ps.promotionToDate IS NOT NULL")
    int clearExpiredPromotions(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(ps) FROM ProductSize ps WHERE ps.promotionToDate < :now AND ps.promotionToDate IS NOT NULL")
    long countExpiredPromotions(@Param("now") LocalDateTime now);
}
