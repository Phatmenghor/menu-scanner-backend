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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category c " +
           "LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.business bus " +
           "LEFT JOIN FETCH p.sizes sz " +
           "WHERE p.id = :id AND p.isDeleted = false " +
           "AND (sz.isDeleted = false OR sz.isDeleted IS NULL)")
    Optional<Product> findByIdWithAllDetails(@Param("id") UUID id);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category c " +
           "LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.business bus " +
           "WHERE p.id IN :productIds AND p.isDeleted = false")
    List<Product> findByIdInWithRelationships(@Param("productIds") List<UUID> productIds);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category c " +
           "LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.business bus " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findByIdWithDetails(@Param("id") UUID id);

    @Query(value = "SELECT DISTINCT p FROM Product p " +
                   "LEFT JOIN FETCH p.category c " +
                   "LEFT JOIN FETCH p.brand b " +
                   "LEFT JOIN FETCH p.business bus " +
                   "WHERE p.isDeleted = false",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.isDeleted = false")
    Page<Product> findAllWithRelationshipsOptimized(Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p " +
                   "LEFT JOIN FETCH p.category c " +
                   "LEFT JOIN FETCH p.brand b " +
                   "LEFT JOIN FETCH p.business bus " +
                   "WHERE p.businessId = :businessId AND p.isDeleted = false",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.businessId = :businessId AND p.isDeleted = false")
    Page<Product> findByBusinessIdWithRelationships(@Param("businessId") UUID businessId, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p " +
                   "LEFT JOIN FETCH p.category c " +
                   "LEFT JOIN FETCH p.brand b " +
                   "LEFT JOIN FETCH p.business bus " +
                   "WHERE p.categoryId = :categoryId AND p.isDeleted = false",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.categoryId = :categoryId AND p.isDeleted = false")
    Page<Product> findByCategoryIdWithRelationships(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p " +
                   "LEFT JOIN FETCH p.category c " +
                   "LEFT JOIN FETCH p.brand b " +
                   "LEFT JOIN FETCH p.business bus " +
                   "WHERE (LOWER(p.name) LIKE LOWER(:searchPattern) " +
                   "OR LOWER(c.name) LIKE LOWER(:searchPattern) " +
                   "OR LOWER(b.name) LIKE LOWER(:searchPattern) " +
                   "OR LOWER(bus.name) LIKE LOWER(:searchPattern)) " +
                   "AND p.isDeleted = false",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p " +
                       "LEFT JOIN p.category c " +
                       "LEFT JOIN p.brand b " +
                       "LEFT JOIN p.business bus " +
                       "WHERE (LOWER(p.name) LIKE LOWER(:searchPattern) " +
                       "OR LOWER(c.name) LIKE LOWER(:searchPattern) " +
                       "OR LOWER(b.name) LIKE LOWER(:searchPattern) " +
                       "OR LOWER(bus.name) LIKE LOWER(:searchPattern)) " +
                       "AND p.isDeleted = false")
    Page<Product> findBySearchWithRelationships(@Param("searchPattern") String searchPattern, Pageable pageable);

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

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.viewCount = COALESCE(p.viewCount, 0) + 1 WHERE p.id = :productId")
    int incrementViewCount(@Param("productId") UUID productId);

    @Modifying
    @Query("UPDATE Product p SET p.favoriteCount = COALESCE(p.favoriteCount, 0) + 1 WHERE p.id = :productId")
    void incrementFavoriteCount(@Param("productId") UUID productId);

    @Modifying
    @Query("UPDATE Product p SET p.favoriteCount = GREATEST(0, COALESCE(p.favoriteCount, 0) - 1) WHERE p.id = :productId")
    void decrementFavoriteCount(@Param("productId") UUID productId);

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
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "INNER JOIN ProductFavorite pf ON p.id = pf.productId " +
           "LEFT JOIN FETCH p.category c " +
           "LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.business bus " +
           "WHERE pf.userId = :userId AND p.isDeleted = false AND pf.isDeleted = false " +
           "ORDER BY pf.createdAt DESC")
    Page<Product> findUserFavorites(@Param("userId") UUID userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.status = :status " +
           "WHERE p.id IN :productIds AND p.isDeleted = false")
    int updateStatusForProducts(@Param("productIds") List<UUID> productIds, 
                               @Param("status") com.emenu.enums.product.ProductStatus status);

    @Query(value = "SELECT DISTINCT p FROM Product p " +
                   "LEFT JOIN FETCH p.category c " +
                   "LEFT JOIN FETCH p.brand b " +
                   "LEFT JOIN FETCH p.business bus " +
                   "WHERE p.status = 'ACTIVE' AND p.isDeleted = false " +
                   "ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Product p " +
                       "WHERE p.status = 'ACTIVE' AND p.isDeleted = false")
    Page<Product> findRecentActiveProducts(Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p " +
                   "LEFT JOIN FETCH p.category c " +
                   "LEFT JOIN FETCH p.brand b " +
                   "LEFT JOIN FETCH p.business bus " +
                   "WHERE p.status = 'ACTIVE' AND p.isDeleted = false " +
                   "ORDER BY p.viewCount DESC, p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Product p " +
                       "WHERE p.status = 'ACTIVE' AND p.isDeleted = false")
    Page<Product> findTopViewedProducts(Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p " +
                   "LEFT JOIN FETCH p.category c " +
                   "LEFT JOIN FETCH p.brand b " +
                   "LEFT JOIN FETCH p.business bus " +
                   "WHERE p.status = 'ACTIVE' AND p.isDeleted = false " +
                   "ORDER BY p.favoriteCount DESC, p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Product p " +
                       "WHERE p.status = 'ACTIVE' AND p.isDeleted = false")
    Page<Product> findTopFavoritedProducts(Pageable pageable);
}