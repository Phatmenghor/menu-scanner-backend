package com.emenu.features.product.repository;

import com.emenu.features.product.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    
    // ================================
    // FIXED: Separate queries to avoid MultipleBagFetchException
    // ================================
    
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdWithDetails(@Param("id") UUID id);

    Optional<Product> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.categoryId = :categoryId AND p.isDeleted = false")
    long countByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.brandId = :brandId AND p.isDeleted = false")
    long countByBrandId(@Param("brandId") UUID brandId);

    // ================================
    // STATISTICS AND UPDATES
    // ================================

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = COALESCE(p.viewCount, 0) + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") UUID productId);

    @Modifying
    @Query("UPDATE Product p SET p.favoriteCount = COALESCE(p.favoriteCount, 0) + 1 WHERE p.id = :productId")
    void incrementFavoriteCount(@Param("productId") UUID productId);

    @Modifying
    @Query("UPDATE Product p SET p.favoriteCount = GREATEST(0, COALESCE(p.favoriteCount, 0) - 1) WHERE p.id = :productId")
    void decrementFavoriteCount(@Param("productId") UUID productId);

    // ================================
    // PROMOTION MANAGEMENT
    // ================================

    @Modifying
    @Query("UPDATE Product p SET p.promotionType = NULL, p.promotionValue = NULL, " +
           "p.promotionFromDate = NULL, p.promotionToDate = NULL " +
           "WHERE p.promotionToDate < :now AND p.promotionToDate IS NOT NULL AND p.isDeleted = false")
    int clearExpiredPromotions(@Param("now") LocalDateTime now);
    
    // ================================
    // FAVORITES INTEGRATION
    // ================================

    @Query("SELECT p FROM Product p " +
           "INNER JOIN ProductFavorite pf ON p.id = pf.productId " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE pf.userId = :userId AND p.isDeleted = false AND pf.isDeleted = false " +
           "ORDER BY pf.createdAt DESC")
    Page<Product> findUserFavorites(@Param("userId") UUID userId, Pageable pageable);
}