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
    
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.sizes " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdWithDetails(@Param("id") UUID id);
    
    Optional<Product> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Product> findByBusinessIdOrderByCreatedAt(@Param("businessId") UUID businessId);
    
    @Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND p.status = :status AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Product> findActiveByBusinessId(@Param("businessId") UUID businessId, @Param("status") ProductStatus status);
    
    @Query("SELECT p FROM Product p WHERE p.categoryId = :categoryId AND p.status = :status AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Product> findByCategoryIdAndStatus(@Param("categoryId") UUID categoryId, @Param("status") ProductStatus status);
    
    @Query("SELECT p FROM Product p WHERE p.brandId = :brandId AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Product> findByBrandId(@Param("brandId") UUID brandId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.businessId = :businessId AND p.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.categoryId = :categoryId AND p.isDeleted = false")
    long countByCategoryId(@Param("categoryId") UUID categoryId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.brandId = :brandId AND p.isDeleted = false")
    long countByBrandId(@Param("brandId") UUID brandId);
    
    boolean existsByNameAndBusinessIdAndIsDeletedFalse(String name, UUID businessId);
    
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

    @Modifying
    @Query("UPDATE Product p SET p.promotionType = NULL, p.promotionValue = NULL, " +
            "p.promotionFromDate = NULL, p.promotionToDate = NULL " +
            "WHERE p.promotionToDate < :now AND p.promotionToDate IS NOT NULL")
    int clearExpiredPromotions(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.promotionToDate < :now AND p.promotionToDate IS NOT NULL")
    long countExpiredPromotions(@Param("now") LocalDateTime now);
}