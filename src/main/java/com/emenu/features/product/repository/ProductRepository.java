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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    
    // ================================
    // OPTIMIZED BASIC QUERIES - Using indexes
    // ================================
    
    /**
     * 🚀 FAST: Single product with collections - Uses primary key
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "LEFT JOIN FETCH p.images i " +
           "LEFT JOIN FETCH p.sizes s " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdWithDetails(@Param("id") UUID id);

    /**
     * 🚀 FAST: Basic product info only - Uses primary key
     */
    Optional<Product> findByIdAndIsDeletedFalse(UUID id);

    /**
     * 🚀 FAST: Business products count - Uses idx_products_business_status_deleted
     */
    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.businessId = :businessId AND p.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);

    /**
     * 🚀 FAST: Category products count - Uses idx_products_category_created_deleted
     */
    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.categoryId = :categoryId AND p.isDeleted = false")
    long countByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * 🚀 FAST: Brand products count - Uses idx_products_brand_created_deleted
     */
    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.brandId = :brandId AND p.isDeleted = false")
    long countByBrandId(@Param("brandId") UUID brandId);

    // ================================
    // BUSINESS-SPECIFIC QUERIES - Using business indexes
    // ================================

    /**
     * 🚀 FAST: Business products with basic info - Uses idx_products_business_created_deleted
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.businessId = :businessId AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC")
    Page<Product> findByBusinessIdOrderByCreatedAtDesc(@Param("businessId") UUID businessId, Pageable pageable);

    /**
     * 🚀 FAST: Active products for business - Uses idx_products_business_status_deleted
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.businessId = :businessId AND p.status = 'ACTIVE' AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC")
    Page<Product> findActiveProductsByBusinessId(@Param("businessId") UUID businessId, Pageable pageable);

    /**
     * 🚀 FAST: Business products by category - Uses idx_products_business_category_deleted
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.businessId = :businessId AND p.categoryId = :categoryId AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC")
    Page<Product> findByBusinessIdAndCategoryId(@Param("businessId") UUID businessId, 
                                                @Param("categoryId") UUID categoryId, 
                                                Pageable pageable);

    // ================================
    // SEARCH QUERIES - Using name index and joins
    // ================================

    /**
     * 🚀 FAST: Search by name - Uses idx_products_name_deleted
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC")
    Page<Product> searchByName(@Param("name") String name, Pageable pageable);

    /**
     * 🚀 FAST: Full text search - Uses multiple indexes efficiently
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category c " +
           "LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.business bus " +
           "WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(bus.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC")
    Page<Product> fullTextSearch(@Param("search") String search, Pageable pageable);

    // ================================
    // STATISTICS AND UPDATES
    // ================================

    /**
     * 🚀 ATOMIC: Increment view count - Uses primary key
     */
    @Modifying
    @Query("UPDATE Product p SET p.viewCount = COALESCE(p.viewCount, 0) + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") UUID productId);

    /**
     * 🚀 ATOMIC: Increment favorite count - Uses primary key
     */
    @Modifying
    @Query("UPDATE Product p SET p.favoriteCount = COALESCE(p.favoriteCount, 0) + 1 WHERE p.id = :productId")
    void incrementFavoriteCount(@Param("productId") UUID productId);

    /**
     * 🚀 ATOMIC: Decrement favorite count - Uses primary key
     */
    @Modifying
    @Query("UPDATE Product p SET p.favoriteCount = GREATEST(0, COALESCE(p.favoriteCount, 0) - 1) WHERE p.id = :productId")
    void decrementFavoriteCount(@Param("productId") UUID productId);

    // ================================
    // PROMOTION MANAGEMENT
    // ================================

    /**
     * 🚀 BATCH: Clear expired promotions - Uses idx_products_promotion_dates
     */
    @Modifying
    @Query("UPDATE Product p SET p.promotionType = NULL, p.promotionValue = NULL, " +
           "p.promotionFromDate = NULL, p.promotionToDate = NULL " +
           "WHERE p.promotionToDate < :now AND p.promotionToDate IS NOT NULL AND p.isDeleted = false")
    int clearExpiredPromotions(@Param("now") LocalDateTime now);

    /**
     * 🚀 BATCH: Clear all promotions for business - Uses idx_products_business_status_deleted
     */
    @Modifying
    @Query("UPDATE Product p SET p.promotionType = NULL, p.promotionValue = NULL, " +
           "p.promotionFromDate = NULL, p.promotionToDate = NULL " +
           "WHERE p.businessId = :businessId AND p.isDeleted = false")
    int clearAllPromotionsForBusiness(@Param("businessId") UUID businessId);

    /**
     * 🚀 FAST: Count promoted products - Uses promotion indexes
     */
    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.promotionType IS NOT NULL AND p.promotionValue IS NOT NULL " +
           "AND (p.promotionFromDate IS NULL OR p.promotionFromDate <= :now) " +
           "AND (p.promotionToDate IS NULL OR p.promotionToDate >= :now) " +
           "AND p.isDeleted = false")
    long countActivePromotions(@Param("now") LocalDateTime now);

    // ================================
    // FAVORITES INTEGRATION
    // ================================

    /**
     * 🚀 FAST: User favorites with product details - Uses favorite indexes
     */
    @Query("SELECT p FROM Product p " +
           "INNER JOIN ProductFavorite pf ON p.id = pf.productId " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE pf.userId = :userId AND p.isDeleted = false AND pf.isDeleted = false " +
           "ORDER BY pf.createdAt DESC")
    Page<Product> findUserFavorites(@Param("userId") UUID userId, Pageable pageable);

    /**
     * 🚀 FAST: Count user favorites - Uses idx_product_favorites_user_deleted
     */
    @Query("SELECT COUNT(pf) FROM ProductFavorite pf " +
           "WHERE pf.userId = :userId AND pf.isDeleted = false")
    long countUserFavorites(@Param("userId") UUID userId);

    // ================================
    // BATCH OPERATIONS
    // ================================

    /**
     * 🚀 BATCH: Find products by IDs - Uses primary key index
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE p.id IN :productIds AND p.isDeleted = false")
    List<Product> findByIdIn(@Param("productIds") List<UUID> productIds);

    /**
     * 🚀 BATCH: Update multiple product statuses - Uses primary key index
     */
    @Modifying
    @Query("UPDATE Product p SET p.status = :status " +
           "WHERE p.id IN :productIds AND p.isDeleted = false")
    int updateStatusForProducts(@Param("productIds") List<UUID> productIds, 
                               @Param("status") com.emenu.enums.product.ProductStatus status);

    // ================================
    // ADMIN QUERIES
    // ================================

    /**
     * 🚀 FAST: Recent products across platform - Uses idx_products_status_created_deleted
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE p.status = 'ACTIVE' AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC")
    Page<Product> findRecentActiveProducts(Pageable pageable);

    /**
     * 🚀 FAST: Top viewed products - Uses view_count (consider adding index)
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE p.status = 'ACTIVE' AND p.isDeleted = false " +
           "ORDER BY p.viewCount DESC, p.createdAt DESC")
    Page<Product> findTopViewedProducts(Pageable pageable);

    /**
     * 🚀 FAST: Top favorited products - Uses favorite_count (consider adding index)
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE p.status = 'ACTIVE' AND p.isDeleted = false " +
           "ORDER BY p.favoriteCount DESC, p.createdAt DESC")
    Page<Product> findTopFavoritedProducts(Pageable pageable);
}