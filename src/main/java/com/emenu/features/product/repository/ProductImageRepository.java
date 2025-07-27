package com.emenu.features.product.repository;

import com.emenu.features.product.models.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.productId = :productId AND pi.isDeleted = false ORDER BY pi.isMain DESC, pi.sortOrder ASC")
    List<ProductImage> findByProductIdOrderByMainAndSort(@Param("productId") UUID productId);
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.productId = :productId AND pi.isMain = true AND pi.isDeleted = false")
    Optional<ProductImage> findMainImageByProductId(@Param("productId") UUID productId);
    
    void deleteByProductIdAndIsDeletedFalse(UUID productId);
}