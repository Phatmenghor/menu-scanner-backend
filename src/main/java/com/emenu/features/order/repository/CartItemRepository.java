package com.emenu.features.order.repository;

import com.emenu.features.order.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    /**
     * Finds a non-deleted cart item by ID
     */
    Optional<CartItem> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds a non-deleted cart item by cart ID, product ID, and optional product size ID
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cartId = :cartId AND ci.productId = :productId AND (:productSizeId IS NULL AND ci.productSizeId IS NULL OR ci.productSizeId = :productSizeId) AND ci.isDeleted = false")
    Optional<CartItem> findByCartIdAndProductIdAndSizeId(@Param("cartId") UUID cartId,
                                                          @Param("productId") UUID productId,
                                                          @Param("productSizeId") UUID productSizeId);

    /**
     * Permanently deletes cart items for deleted products
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.productId IN (SELECT p.id FROM Product p WHERE p.isDeleted = true)")
    int deleteCartItemsForDeletedProducts();

    /**
     * Permanently deletes cart items for inactive products
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.productId IN (SELECT p.id FROM Product p WHERE p.status != 'ACTIVE')")
    int deleteCartItemsForInactiveProducts();

    /**
     * Permanently deletes cart items for deleted product sizes
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.productSizeId IN (SELECT ps.id FROM ProductSize ps WHERE ps.isDeleted = true)")
    int deleteCartItemsForDeletedProductSizes();

    /**
     * Permanently deletes cart items older than the specified cutoff date
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.createdAt < :cutoffDate")
    int deleteOldCartItems(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Counts all non-deleted cart items
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.isDeleted = false")
    long countActiveCartItems();

    /**
     * Counts cart items older than the specified cutoff date
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.createdAt < :cutoffDate")
    long countOldCartItems(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get total quantities for products in user's cart for a specific business.
     * Returns a map of productId to total quantity across all cart items for that product.
     */
    @Query("""
            SELECT ci.productId as productId, SUM(ci.quantity) as totalQuantity
            FROM CartItem ci
            JOIN Cart c ON ci.cartId = c.id
            WHERE c.userId = :userId
            AND c.businessId = :businessId
            AND ci.productId IN :productIds
            AND ci.isDeleted = false
            AND c.isDeleted = false
            GROUP BY ci.productId
            """)
    List<Map<String, Object>> getProductQuantitiesInCart(@Param("userId") UUID userId,
                                                          @Param("businessId") UUID businessId,
                                                          @Param("productIds") List<UUID> productIds);
}