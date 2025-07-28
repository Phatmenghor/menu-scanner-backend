package com.emenu.features.order.repository;

import com.emenu.features.order.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    
    Optional<CartItem> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cartId = :cartId AND ci.productId = :productId AND (:productSizeId IS NULL AND ci.productSizeId IS NULL OR ci.productSizeId = :productSizeId) AND ci.isDeleted = false")
    Optional<CartItem> findByCartIdAndProductIdAndSizeId(@Param("cartId") UUID cartId,
                                                          @Param("productId") UUID productId, 
                                                          @Param("productSizeId") UUID productSizeId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cartId = :cartId AND ci.isDeleted = false")
    List<CartItem> findByCartIdAndIsDeletedFalse(@Param("cartId") UUID cartId);
    
    // HARD DELETE methods - these will permanently remove cart items
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.productId IN (SELECT p.id FROM Product p WHERE p.isDeleted = true)")
    int deleteCartItemsForDeletedProducts();

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.productId IN (SELECT p.id FROM Product p WHERE p.status != 'ACTIVE')")
    int deleteCartItemsForInactiveProducts();

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.productSizeId IN (SELECT ps.id FROM ProductSize ps WHERE ps.isDeleted = true)")
    int deleteCartItemsForDeletedProductSizes();

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.createdAt < :cutoffDate")
    int deleteOldCartItems(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.isDeleted = false")
    long countActiveCartItems();

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.createdAt < :cutoffDate")
    long countOldCartItems(@Param("cutoffDate") LocalDateTime cutoffDate);
}