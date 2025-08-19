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
    
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "LEFT JOIN FETCH p.images i " +
           "LEFT JOIN FETCH p.sizes s " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdWithDetails(@Param("id") UUID id);

    Optional<Product> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.businessId = :businessId AND p.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);

    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.categoryId = :categoryId AND p.isDeleted = false")
    long countByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.brandId = :brandId AND p.isDeleted = false")
    long countByBrandId(@Param("brandId") UUID brandId);

    // ================================
    // BUSINESS-SPECIFIC QUERIES - Using business indexes
    // ================================

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.businessId = :businessId AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC")
    Page<Product> findByBusinessIdOrderByCreatedAtDesc(@Param("businessId") UUID businessId, Pageable pageable);

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE p.businessId = :businessId AND p.status = 'ACTIVE' AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC")
    Page<Product> findActiveProductsByBusinessId(@Param("businessId") UUID businessId, Pageable pageable);

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

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC")
    Page<Product> searchByName(@Param("name") String name, Pageable pageable);

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

    @Modifying
    @Query("UPDATE Product p SET p.promotionType = NULL, p.promotionValue = NULL, " +
           "p.promotionFromDate = NULL, p.promotionToDate = NULL " +
           "WHERE p.businessId = :businessId AND p.isDeleted = false")
    int clearAllPromotionsForBusiness(@Param("businessId") UUID businessId);

    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.promotionType IS NOT NULL AND p.promotionValue IS NOT NULL " +
           "AND (p.promotionFromDate IS NULL OR p.promotionFromDate <= :now) " +
           "AND (p.promotionToDate IS NULL OR p.promotionToDate >= :now) " +
           "AND p.isDeleted = false")
    long countActivePromotions(@Param("now") LocalDateTime now);

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

    @Query("SELECT COUNT(pf) FROM ProductFavorite pf " +
           "WHERE pf.userId = :userId AND pf.isDeleted = false")
    long countUserFavorites(@Param("userId") UUID userId);

    // ================================
    // BATCH OPERATIONS
    // ================================

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE p.id IN :productIds AND p.isDeleted = false")
    List<Product> findByIdIn(@Param("productIds") List<UUID> productIds);

    @Modifying
    @Query("UPDATE Product p SET p.status = :status " +
           "WHERE p.id IN :productIds AND p.isDeleted = false")
    int updateStatusForProducts(@Param("productIds") List<UUID> productIds, 
                               @Param("status") com.emenu.enums.product.ProductStatus status);

    // ================================
    // ADMIN QUERIES
    // ================================

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE p.status = 'ACTIVE' AND p.isDeleted = false " +
           "ORDER BY p.createdAt DESC")
    Page<Product> findRecentActiveProducts(Pageable pageable);

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE p.status = 'ACTIVE' AND p.isDeleted = false " +
           "ORDER BY p.viewCount DESC, p.createdAt DESC")
    Page<Product> findTopViewedProducts(Pageable pageable);

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.business " +
           "WHERE p.status = 'ACTIVE' AND p.isDeleted = false " +
           "ORDER BY p.favoriteCount DESC, p.createdAt DESC")
    Page<Product> findTopFavoritedProducts(Pageable pageable);
}