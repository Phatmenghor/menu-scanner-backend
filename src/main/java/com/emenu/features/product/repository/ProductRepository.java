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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    Optional<Product> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.categoryId = :categoryId AND p.isDeleted = false")
    long countByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.brandId = :brandId AND p.isDeleted = false")
    long countByBrandId(@Param("brandId") UUID brandId);

    // ⚡ NEW: Batch count by category IDs (single query instead of N queries)
    @Query("SELECT p.categoryId as categoryId, COUNT(p) as count " +
           "FROM Product p " +
           "WHERE p.categoryId IN :categoryIds AND p.isDeleted = false " +
           "GROUP BY p.categoryId")
    List<Object[]> countByCategoryIdsRaw(@Param("categoryIds") List<UUID> categoryIds);

    default Map<UUID, Long> countByCategoryIds(List<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }
        return countByCategoryIdsRaw(categoryIds).stream()
                .collect(Collectors.toMap(
                        result -> (UUID) result[0],
                        result -> (Long) result[1]
                ));
    }

    // ⚡ NEW: Batch count ACTIVE products by category IDs
    @Query("SELECT p.categoryId as categoryId, COUNT(p) as count " +
           "FROM Product p " +
           "WHERE p.categoryId IN :categoryIds AND p.isDeleted = false AND p.status = 'ACTIVE' " +
           "GROUP BY p.categoryId")
    List<Object[]> countActiveByCategoryIdsRaw(@Param("categoryIds") List<UUID> categoryIds);

    default Map<UUID, Long> countActiveByCategoryIds(List<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }
        return countActiveByCategoryIdsRaw(categoryIds).stream()
                .collect(Collectors.toMap(
                        result -> (UUID) result[0],
                        result -> (Long) result[1]
                ));
    }

    // ⚡ NEW: Batch count by brand IDs (single query instead of N queries)
    @Query("SELECT p.brandId as brandId, COUNT(p) as count " +
           "FROM Product p " +
           "WHERE p.brandId IN :brandIds AND p.isDeleted = false " +
           "GROUP BY p.brandId")
    List<Object[]> countByBrandIdsRaw(@Param("brandIds") List<UUID> brandIds);

    default Map<UUID, Long> countByBrandIds(List<UUID> brandIds) {
        if (brandIds == null || brandIds.isEmpty()) {
            return Map.of();
        }
        return countByBrandIdsRaw(brandIds).stream()
                .collect(Collectors.toMap(
                        result -> (UUID) result[0],
                        result -> (Long) result[1]
                ));
    }

    // ⚡ NEW: Batch count ACTIVE products by brand IDs
    @Query("SELECT p.brandId as brandId, COUNT(p) as count " +
           "FROM Product p " +
           "WHERE p.brandId IN :brandIds AND p.isDeleted = false AND p.status = 'ACTIVE' " +
           "GROUP BY p.brandId")
    List<Object[]> countActiveByBrandIdsRaw(@Param("brandIds") List<UUID> brandIds);

    default Map<UUID, Long> countActiveByBrandIds(List<UUID> brandIds) {
        if (brandIds == null || brandIds.isEmpty()) {
            return Map.of();
        }
        return countActiveByBrandIdsRaw(brandIds).stream()
                .collect(Collectors.toMap(
                        result -> (UUID) result[0],
                        result -> (Long) result[1]
                ));
    }

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
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "INNER JOIN ProductFavorite pf ON p.id = pf.productId " +
           "WHERE pf.userId = :userId AND p.isDeleted = false AND pf.isDeleted = false " +
           "ORDER BY pf.createdAt DESC")
    Page<Product> findUserFavorites(@Param("userId") UUID userId, Pageable pageable);
}