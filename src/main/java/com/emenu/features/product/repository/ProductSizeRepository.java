package com.emenu.features.product.repository;

import com.emenu.features.product.models.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
