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
    
    // ================================
    // FAST LISTING QUERIES - OPTIMIZED FOR PERFORMANCE
    // ================================
    
    /**
     * Fast native query for product listings with calculated pricing
     * Includes product-level and size-level promotion calculations
     */
    @Query(value = """
        SELECT DISTINCT p.id, p.business_id, p.category_id, p.brand_id, p.name, p.description, 
               p.status, p.price, p.promotion_type, p.promotion_value, 
               p.view_count, p.favorite_count, p.created_at, p.updated_at,
               b.name as business_name, c.name as category_name, br.name as brand_name,
               (SELECT pi.image_url FROM product_images pi 
                WHERE pi.product_id = p.id AND pi.image_type = 'MAIN' 
                AND pi.is_deleted = false LIMIT 1) as main_image_url,
               (SELECT COUNT(*) > 0 FROM product_sizes ps 
                WHERE ps.product_id = p.id AND ps.is_deleted = false) as has_sizes,
               (SELECT CASE 
                    WHEN COUNT(*) = 0 THEN 
                        CASE 
                            WHEN p.promotion_type IS NOT NULL 
                            AND (p.promotion_from_date IS NULL OR p.promotion_from_date <= NOW())
                            AND (p.promotion_to_date IS NULL OR p.promotion_to_date >= NOW())
                            THEN 
                                CASE 
                                    WHEN p.promotion_type = 'PERCENTAGE' 
                                    THEN p.price - (p.price * p.promotion_value / 100)
                                    WHEN p.promotion_type = 'FIXED_AMOUNT' 
                                    THEN GREATEST(0, p.price - p.promotion_value)
                                    ELSE p.price
                                END
                            ELSE p.price
                        END
                    ELSE MIN(CASE 
                        WHEN ps.promotion_type IS NOT NULL 
                        AND (ps.promotion_from_date IS NULL OR ps.promotion_from_date <= NOW())
                        AND (ps.promotion_to_date IS NULL OR ps.promotion_to_date >= NOW())
                        THEN 
                            CASE 
                                WHEN ps.promotion_type = 'PERCENTAGE' 
                                THEN ps.price - (ps.price * ps.promotion_value / 100)
                                WHEN ps.promotion_type = 'FIXED_AMOUNT' 
                                THEN GREATEST(0, ps.price - ps.promotion_value)
                                ELSE ps.price
                            END
                        ELSE ps.price
                    END)
                END
                FROM product_sizes ps 
                WHERE ps.product_id = p.id AND ps.is_deleted = false) as display_price,
               (SELECT CASE 
                    WHEN COUNT(*) = 0 THEN 
                        CASE 
                            WHEN p.promotion_type IS NOT NULL 
                            AND (p.promotion_from_date IS NULL OR p.promotion_from_date <= NOW())
                            AND (p.promotion_to_date IS NULL OR p.promotion_to_date >= NOW())
                            THEN TRUE ELSE FALSE
                        END
                    ELSE COUNT(*) > 0
                END
                FROM product_sizes ps 
                WHERE ps.product_id = p.id AND ps.is_deleted = false
                AND ps.promotion_type IS NOT NULL 
                AND (ps.promotion_from_date IS NULL OR ps.promotion_from_date <= NOW())
                AND (ps.promotion_to_date IS NULL OR ps.promotion_to_date >= NOW())) as has_active_promotion
        FROM products p
        LEFT JOIN businesses b ON p.business_id = b.id
        LEFT JOIN categories c ON p.category_id = c.id
        LEFT JOIN brands br ON p.brand_id = br.id
        WHERE p.is_deleted = false
        AND (:businessId IS NULL OR p.business_id = :businessId)
        AND (:categoryId IS NULL OR p.category_id = :categoryId)
        AND (:brandId IS NULL OR p.brand_id = :brandId)
        AND (:status IS NULL OR p.status = CAST(:status AS VARCHAR))
        AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY p.created_at DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> findProductsForListing(
            @Param("businessId") UUID businessId,
            @Param("categoryId") UUID categoryId,
            @Param("brandId") UUID brandId,
            @Param("status") String status,
            @Param("search") String search,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
    
    /**
     * Count query for pagination
     */
    @Query(value = """
        SELECT COUNT(DISTINCT p.id)
        FROM products p
        WHERE p.is_deleted = false
        AND (:businessId IS NULL OR p.business_id = :businessId)
        AND (:categoryId IS NULL OR p.category_id = :categoryId)
        AND (:brandId IS NULL OR p.brand_id = :brandId)
        AND (:status IS NULL OR p.status = CAST(:status AS VARCHAR))
        AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
        """, nativeQuery = true)
    long countProductsForListing(
            @Param("businessId") UUID businessId,
            @Param("categoryId") UUID categoryId,
            @Param("brandId") UUID brandId,
            @Param("status") String status,
            @Param("search") String search
    );
    
    /**
     * Fast user favorites query
     */
    @Query(value = """
        SELECT p.id, p.business_id, p.category_id, p.brand_id, p.name, p.description, 
               p.status, p.price, p.promotion_type, p.promotion_value, 
               p.view_count, p.favorite_count, p.created_at, p.updated_at,
               b.name as business_name, c.name as category_name, br.name as brand_name,
               (SELECT pi.image_url FROM product_images pi 
                WHERE pi.product_id = p.id AND pi.image_type = 'MAIN' 
                AND pi.is_deleted = false LIMIT 1) as main_image_url,
               (SELECT COUNT(*) > 0 FROM product_sizes ps 
                WHERE ps.product_id = p.id AND ps.is_deleted = false) as has_sizes,
               (SELECT CASE 
                    WHEN COUNT(*) = 0 THEN 
                        CASE 
                            WHEN p.promotion_type IS NOT NULL 
                            AND (p.promotion_from_date IS NULL OR p.promotion_from_date <= NOW())
                            AND (p.promotion_to_date IS NULL OR p.promotion_to_date >= NOW())
                            THEN 
                                CASE 
                                    WHEN p.promotion_type = 'PERCENTAGE' 
                                    THEN p.price - (p.price * p.promotion_value / 100)
                                    WHEN p.promotion_type = 'FIXED_AMOUNT' 
                                    THEN GREATEST(0, p.price - p.promotion_value)
                                    ELSE p.price
                                END
                            ELSE p.price
                        END
                    ELSE MIN(CASE 
                        WHEN ps.promotion_type IS NOT NULL 
                        AND (ps.promotion_from_date IS NULL OR ps.promotion_from_date <= NOW())
                        AND (ps.promotion_to_date IS NULL OR ps.promotion_to_date >= NOW())
                        THEN 
                            CASE 
                                WHEN ps.promotion_type = 'PERCENTAGE' 
                                THEN ps.price - (ps.price * ps.promotion_value / 100)
                                WHEN ps.promotion_type = 'FIXED_AMOUNT' 
                                THEN GREATEST(0, ps.price - ps.promotion_value)
                                ELSE ps.price
                            END
                        ELSE ps.price
                    END)
                END
                FROM product_sizes ps 
                WHERE ps.product_id = p.id AND ps.is_deleted = false) as display_price,
               (SELECT CASE 
                    WHEN COUNT(*) = 0 THEN 
                        CASE 
                            WHEN p.promotion_type IS NOT NULL 
                            AND (p.promotion_from_date IS NULL OR p.promotion_from_date <= NOW())
                            AND (p.promotion_to_date IS NULL OR p.promotion_to_date >= NOW())
                            THEN TRUE ELSE FALSE
                        END
                    ELSE COUNT(*) > 0
                END
                FROM product_sizes ps 
                WHERE ps.product_id = p.id AND ps.is_deleted = false
                AND ps.promotion_type IS NOT NULL 
                AND (ps.promotion_from_date IS NULL OR ps.promotion_from_date <= NOW())
                AND (ps.promotion_to_date IS NULL OR ps.promotion_to_date >= NOW())) as has_active_promotion
        FROM products p
        INNER JOIN product_favorites pf ON p.id = pf.product_id
        LEFT JOIN businesses b ON p.business_id = b.id
        LEFT JOIN categories c ON p.category_id = c.id
        LEFT JOIN brands br ON p.brand_id = br.id
        WHERE p.is_deleted = false AND pf.is_deleted = false
        AND pf.user_id = :userId
        ORDER BY pf.created_at DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> findUserFavoriteProducts(
            @Param("userId") UUID userId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
    
    @Query("SELECT COUNT(pf) FROM ProductFavorite pf " +
           "WHERE pf.userId = :userId AND pf.isDeleted = false")
    long countUserFavorites(@Param("userId") UUID userId);
    
    // ================================
    // DETAILED QUERIES FOR SINGLE PRODUCT VIEW
    // ================================
    
    /**
     * Full product details with all collections - for single product view
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "LEFT JOIN FETCH p.images i " +
           "LEFT JOIN FETCH p.sizes s " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdWithAllDetails(@Param("id") UUID id);
    
    // Basic fetch with main relationships only
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdWithDetails(@Param("id") UUID id);
    
    Optional<Product> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.sizes " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdWithSizes(@Param("id") UUID id);
    
    // ================================
    // STATISTICS AND UPDATES
    // ================================
    
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