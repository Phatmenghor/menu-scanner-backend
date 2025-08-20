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

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    
    // ✅ DETAIL QUERY: Only for single product detail with relationships
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category c " +
           "LEFT JOIN FETCH p.brand b " +
           "LEFT JOIN FETCH p.business bus " +
           "LEFT JOIN FETCH p.sizes sz " +
           "WHERE p.id = :id AND p.isDeleted = false " +
           "AND (sz.isDeleted = false OR sz.isDeleted IS NULL)")
    Optional<Product> findByIdWithAllDetails(@Param("id") UUID id);

    // ✅ SIMPLE QUERIES: Basic operations without relationships
    Optional<Product> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.categoryId = :categoryId AND p.isDeleted = false")
    long countByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.brandId = :brandId AND p.isDeleted = false")
    long countByBrandId(@Param("brandId") UUID brandId);

    // ✅ UPDATE OPERATIONS: Simple field updates
    @Modifying
    @Query("UPDATE Product p SET p.viewCount = COALESCE(p.viewCount, 0) + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") UUID productId);

    @Modifying
    @Query("UPDATE Product p SET p.favoriteCount = COALESCE(p.favoriteCount, 0) + 1 WHERE p.id = :productId")
    void incrementFavoriteCount(@Param("productId") UUID productId);

    @Modifying
    @Query("UPDATE Product p SET p.favoriteCount = GREATEST(0, COALESCE(p.favoriteCount, 0) - 1) WHERE p.id = :productId")
    void decrementFavoriteCount(@Param("productId") UUID productId);
    
    // ✅ FAVORITES: Optimized favorites query (no relationships in main query)
    @Query("SELECT DISTINCT p FROM Product p " +
           "INNER JOIN ProductFavorite pf ON p.id = pf.productId " +
           "WHERE pf.userId = :userId AND p.isDeleted = false AND pf.isDeleted = false " +
           "ORDER BY pf.createdAt DESC")
    Page<Product> findUserFavorites(@Param("userId") UUID userId, Pageable pageable);
}