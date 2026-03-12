package com.emenu.features.main.repository;

import com.emenu.features.main.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;

import com.emenu.enums.product.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Find product by ID with all related details (category, brand, business, sizes)
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category c " +
           "LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.business bus " +
           "LEFT JOIN FETCH p.sizes sz " +
           "WHERE p.id = :id AND p.isDeleted = false " +
           "AND (sz.isDeleted = false OR sz.isDeleted IS NULL)")
    Optional<Product> findByIdWithAllDetails(@Param("id") UUID id);

    Optional<Product> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Count active products in a category
     */
    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.categoryId = :categoryId AND p.isDeleted = false")
    long countByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * Count active products for a brand
     */
    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.brandId = :brandId AND p.isDeleted = false")
    long countByBrandId(@Param("brandId") UUID brandId);

    /**
     * Increment product view count
     */
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.viewCount = COALESCE(p.viewCount, 0) + 1 WHERE p.id = :productId")
    int incrementViewCount(@Param("productId") UUID productId);

    /**
     * Increment product favorite count
     */
    @Modifying
    @Query("UPDATE Product p SET p.favoriteCount = COALESCE(p.favoriteCount, 0) + 1 WHERE p.id = :productId")
    void incrementFavoriteCount(@Param("productId") UUID productId);

    /**
     * Decrement product favorite count (minimum 0)
     */
    @Modifying
    @Query("UPDATE Product p SET p.favoriteCount = GREATEST(0, COALESCE(p.favoriteCount, 0) - 1) WHERE p.id = :productId")
    void decrementFavoriteCount(@Param("productId") UUID productId);
    
    /**
     * Find all favorited products for a specific user
     */
    @Query("SELECT p FROM Product p " +
           "INNER JOIN ProductFavorite pf ON p.id = pf.productId " +
           "WHERE pf.userId = :userId AND p.isDeleted = false AND pf.isDeleted = false")
    Page<Product> findUserFavorites(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find all favorited products for a specific user within a business
     */
    @Query("SELECT p FROM Product p " +
           "INNER JOIN ProductFavorite pf ON p.id = pf.productId " +
           "WHERE pf.userId = :userId AND p.businessId = :businessId " +
           "AND p.isDeleted = false AND pf.isDeleted = false")
    Page<Product> findUserFavoritesByBusiness(@Param("userId") UUID userId,
                                              @Param("businessId") UUID businessId,
                                              Pageable pageable);

    /**
     * Find all products with dynamic filtering - paginated
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN p.category c " +
           "LEFT JOIN p.brand b " +
           "LEFT JOIN p.business bus " +
           "WHERE p.isDeleted = false " +
           "AND (:businessId IS NULL OR p.businessId = :businessId) " +
           "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
           "AND (:brandId IS NULL OR p.brandId = :brandId) " +
           "AND (:statuses IS NULL OR p.status IN :statuses) " +
           "AND (:needsPromotion IS NULL OR (" +
           "     EXISTS (SELECT sz FROM ProductSize sz WHERE sz.productId = p.id AND sz.isDeleted = false " +
           "        AND sz.promotionValue IS NOT NULL AND sz.promotionType IS NOT NULL " +
           "        AND (sz.promotionFromDate IS NULL OR sz.promotionFromDate <= CURRENT_DATE) " +
           "        AND (sz.promotionToDate IS NULL OR sz.promotionToDate >= CURRENT_DATE)) " +
           "     OR (NOT EXISTS (SELECT sz FROM ProductSize sz WHERE sz.productId = p.id AND sz.isDeleted = false) " +
           "         AND p.promotionValue IS NOT NULL AND p.promotionType IS NOT NULL " +
           "         AND (p.promotionFromDate IS NULL OR p.promotionFromDate <= CURRENT_DATE) " +
           "         AND (p.promotionToDate IS NULL OR p.promotionToDate >= CURRENT_DATE)))) " +
           "AND (:needsNoPromotion IS NULL OR (" +
           "     NOT EXISTS (SELECT sz FROM ProductSize sz WHERE sz.productId = p.id AND sz.isDeleted = false " +
           "        AND sz.promotionValue IS NOT NULL AND sz.promotionType IS NOT NULL " +
           "        AND (sz.promotionFromDate IS NULL OR sz.promotionFromDate <= CURRENT_DATE) " +
           "        AND (sz.promotionToDate IS NULL OR sz.promotionToDate >= CURRENT_DATE)) " +
           "     AND (EXISTS (SELECT sz FROM ProductSize sz WHERE sz.productId = p.id AND sz.isDeleted = false) " +
           "          OR (p.promotionValue IS NULL OR p.promotionType IS NULL " +
           "              OR (p.promotionFromDate IS NOT NULL AND p.promotionFromDate > CURRENT_DATE) " +
           "              OR (p.promotionToDate IS NOT NULL AND p.promotionToDate < CURRENT_DATE))))) " +
           "AND (:minPrice IS NULL OR p.displayPrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.displayPrice <= :maxPrice) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findAllWithFilters(
        @Param("businessId") UUID businessId,
        @Param("categoryId") UUID categoryId,
        @Param("brandId") UUID brandId,
        @Param("statuses") List<ProductStatus> statuses,
        @Param("needsPromotion") Boolean needsPromotion,
        @Param("needsNoPromotion") Boolean needsNoPromotion,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("search") String search,
        Pageable pageable
    );

    /**
     * Find all products with dynamic filtering - non-paginated
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN p.category c " +
           "LEFT JOIN p.brand b " +
           "LEFT JOIN p.business bus " +
           "WHERE p.isDeleted = false " +
           "AND (:businessId IS NULL OR p.businessId = :businessId) " +
           "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
           "AND (:brandId IS NULL OR p.brandId = :brandId) " +
           "AND (:statuses IS NULL OR p.status IN :statuses) " +
           "AND (:needsPromotion IS NULL OR (" +
           "     EXISTS (SELECT sz FROM ProductSize sz WHERE sz.productId = p.id AND sz.isDeleted = false " +
           "        AND sz.promotionValue IS NOT NULL AND sz.promotionType IS NOT NULL " +
           "        AND (sz.promotionFromDate IS NULL OR sz.promotionFromDate <= CURRENT_DATE) " +
           "        AND (sz.promotionToDate IS NULL OR sz.promotionToDate >= CURRENT_DATE)) " +
           "     OR (NOT EXISTS (SELECT sz FROM ProductSize sz WHERE sz.productId = p.id AND sz.isDeleted = false) " +
           "         AND p.promotionValue IS NOT NULL AND p.promotionType IS NOT NULL " +
           "         AND (p.promotionFromDate IS NULL OR p.promotionFromDate <= CURRENT_DATE) " +
           "         AND (p.promotionToDate IS NULL OR p.promotionToDate >= CURRENT_DATE)))) " +
           "AND (:needsNoPromotion IS NULL OR (" +
           "     NOT EXISTS (SELECT sz FROM ProductSize sz WHERE sz.productId = p.id AND sz.isDeleted = false " +
           "        AND sz.promotionValue IS NOT NULL AND sz.promotionType IS NOT NULL " +
           "        AND (sz.promotionFromDate IS NULL OR sz.promotionFromDate <= CURRENT_DATE) " +
           "        AND (sz.promotionToDate IS NULL OR sz.promotionToDate >= CURRENT_DATE)) " +
           "     AND (EXISTS (SELECT sz FROM ProductSize sz WHERE sz.productId = p.id AND sz.isDeleted = false) " +
           "          OR (p.promotionValue IS NULL OR p.promotionType IS NULL " +
           "              OR (p.promotionFromDate IS NOT NULL AND p.promotionFromDate > CURRENT_DATE) " +
           "              OR (p.promotionToDate IS NOT NULL AND p.promotionToDate < CURRENT_DATE))))) " +
           "AND (:minPrice IS NULL OR p.displayPrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.displayPrice <= :maxPrice) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Product> findAllWithFilters(
        @Param("businessId") UUID businessId,
        @Param("categoryId") UUID categoryId,
        @Param("brandId") UUID brandId,
        @Param("statuses") List<ProductStatus> statuses,
        @Param("needsPromotion") Boolean needsPromotion,
        @Param("needsNoPromotion") Boolean needsNoPromotion,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("search") String search,
        Sort sort
    );

    /**
     * Clear display promotion fields for products WITHOUT sizes whose promotion has expired
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value =
        "UPDATE products SET " +
        "    has_active_promotion = false, " +
        "    display_promotion_type = NULL, " +
        "    display_promotion_value = NULL, " +
        "    display_promotion_from_date = NULL, " +
        "    display_promotion_to_date = NULL, " +
        "    display_price = price, " +
        "    display_origin_price = price " +
        "WHERE is_deleted = false " +
        "  AND has_sizes = false " +
        "  AND has_active_promotion = true " +
        "  AND ( " +
        "      promotion_value IS NULL " +
        "      OR promotion_type IS NULL " +
        "      OR (promotion_from_date IS NOT NULL AND promotion_from_date::date > CURRENT_DATE) " +
        "      OR (promotion_to_date   IS NOT NULL AND promotion_to_date::date   < CURRENT_DATE) " +
        "  )")
    int clearExpiredPromotionsForProductsWithoutSizes();

    /**
     * Clear display promotion fields for products WITH sizes where no size has an active promotion
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value =
        "UPDATE products p SET " +
        "    has_active_promotion = false, " +
        "    display_promotion_type = NULL, " +
        "    display_promotion_value = NULL, " +
        "    display_promotion_from_date = NULL, " +
        "    display_promotion_to_date = NULL, " +
        "    display_price        = (SELECT MIN(ps.price) FROM product_sizes ps WHERE ps.product_id = p.id AND ps.is_deleted = false), " +
        "    display_origin_price = (SELECT MIN(ps.price) FROM product_sizes ps WHERE ps.product_id = p.id AND ps.is_deleted = false) " +
        "WHERE p.is_deleted = false " +
        "  AND p.has_sizes = true " +
        "  AND p.has_active_promotion = true " +
        "  AND NOT EXISTS ( " +
        "      SELECT 1 FROM product_sizes ps " +
        "      WHERE ps.product_id = p.id " +
        "        AND ps.is_deleted = false " +
        "        AND ps.promotion_value IS NOT NULL " +
        "        AND ps.promotion_type  IS NOT NULL " +
        "        AND (ps.promotion_from_date IS NULL OR ps.promotion_from_date::date <= CURRENT_DATE) " +
        "        AND (ps.promotion_to_date   IS NULL OR ps.promotion_to_date::date   >= CURRENT_DATE) " +
        "  )")
    int clearExpiredPromotionsForProductsWithSizes();
}