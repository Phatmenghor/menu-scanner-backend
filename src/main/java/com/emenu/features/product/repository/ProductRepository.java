// Enhanced ProductRepository.java with better update support
package com.emenu.features.product.repository;

import com.emenu.enums.product.ProductStatus;
import com.emenu.features.product.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    
    // Basic fetch with main relationships only
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdWithDetails(@Param("id") UUID id);
    
    @Query("SELECT p FROM Product p " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdBasic(@Param("id") UUID id);
    
    Optional<Product> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdWithImages(@Param("id") UUID id);
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.sizes " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdWithSizes(@Param("id") UUID id);
    
    // Increment view count
    @Modifying
    @Query("UPDATE Product p SET p.viewCount = COALESCE(p.viewCount, 0) + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") UUID productId);
    
    // Update favorite count
    @Modifying
    @Query("UPDATE Product p SET p.favoriteCount = COALESCE(p.favoriteCount, 0) + 1 WHERE p.id = :productId")
    void incrementFavoriteCount(@Param("productId") UUID productId);
    
    @Modifying
    @Query("UPDATE Product p SET p.favoriteCount = GREATEST(0, COALESCE(p.favoriteCount, 0) - 1) WHERE p.id = :productId")
    void decrementFavoriteCount(@Param("productId") UUID productId);

    // Promotion management
    @Modifying
    @Query("UPDATE Product p SET p.promotionType = NULL, p.promotionValue = NULL, " +
            "p.promotionFromDate = NULL, p.promotionToDate = NULL " +
            "WHERE p.promotionToDate < :now AND p.promotionToDate IS NOT NULL")
    int clearExpiredPromotions(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE Product p SET p.promotionType = NULL, p.promotionValue = NULL, " +
            "p.promotionFromDate = NULL, p.promotionToDate = NULL " +
            "WHERE p.businessId = :businessId AND p.isDeleted = false")
    int clearAllPromotionsForBusiness(@Param("businessId") UUID businessId);
    
    // Statistics
    @Query("SELECT COUNT(p) FROM Product p WHERE p.categoryId = :categoryId AND p.isDeleted = false")
    long countByCategoryId(@Param("categoryId") UUID categoryId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.brandId = :brandId AND p.isDeleted = false")
    long countByBrandId(@Param("brandId") UUID brandId);
}