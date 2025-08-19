package com.emenu.features.product.repository;

import com.emenu.features.product.models.ProductFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, UUID> {
    
    /**
     * 🚀 FAST: Check if favorited - Uses idx_product_favorites_user_product_deleted
     */
    boolean existsByUserIdAndProductIdAndIsDeletedFalse(UUID userId, UUID productId);
    
    /**
     * 🚀 FAST: Get user favorites - Uses idx_product_favorites_user_created_deleted
     */
    @Query("SELECT pf FROM ProductFavorite pf " +
           "WHERE pf.userId = :userId AND pf.isDeleted = false " +
           "ORDER BY pf.createdAt DESC")
    Page<ProductFavorite> findByUserIdAndIsDeletedFalse(@Param("userId") UUID userId, Pageable pageable);
    
    /**
     * 🚀 BATCH: Get favorite product IDs for user and specific products
     */
    @Query("SELECT pf.productId FROM ProductFavorite pf " +
           "WHERE pf.userId = :userId AND pf.productId IN :productIds AND pf.isDeleted = false")
    List<UUID> findFavoriteProductIdsByUserIdAndProductIds(@Param("userId") UUID userId,
                                                           @Param("productIds") List<UUID> productIds);
    
    /**
     * 🚀 FAST: Remove favorite - Uses unique constraint index
     */
    @Modifying
    @Query("DELETE FROM ProductFavorite pf WHERE pf.userId = :userId AND pf.productId = :productId")
    void deleteByUserIdAndProductId(@Param("userId") UUID userId, @Param("productId") UUID productId);
    
    /**
     * 🚀 BATCH: Remove all user favorites - Uses idx_product_favorites_user_deleted
     */
    @Modifying
    @Query("DELETE FROM ProductFavorite pf WHERE pf.userId = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);
    
    /**
     * 🚀 FAST: Count user favorites - Uses idx_product_favorites_user_deleted
     */
    @Query("SELECT COUNT(pf) FROM ProductFavorite pf WHERE pf.userId = :userId AND pf.isDeleted = false")
    long countByUserIdAndIsDeletedFalse(@Param("userId") UUID userId);
    
    /**
     * 🚀 FAST: Count product favorites - Uses idx_product_favorites_product_deleted
     */
    @Query("SELECT COUNT(pf) FROM ProductFavorite pf WHERE pf.productId = :productId AND pf.isDeleted = false")
    long countByProductIdAndIsDeletedFalse(@Param("productId") UUID productId);
}