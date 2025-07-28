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
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cartId = :cartId AND ci.productId = :productId AND ci.productSizeId = :productSizeId AND ci.isDeleted = false")
    Optional<CartItem> findByCartIdAndProductIdAndSizeId(@Param("cartId") UUID cartId,
                                                          @Param("productId") UUID productId, 
                                                          @Param("productSizeId") UUID productSizeId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cartId = :cartId AND ci.isDeleted = false")
    List<CartItem> findByCartIdAndIsDeletedFalse(@Param("cartId") UUID cartId);
    
    void deleteByCartIdAndIsDeletedFalse(UUID cartId);

    @Modifying
    @Query("UPDATE CartItem ci SET ci.isDeleted = true, ci.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE ci.productId IN (SELECT p.id FROM Product p WHERE p.isDeleted = true) " +
            "AND ci.isDeleted = false")
    int deleteCartItemsForDeletedProducts();

    @Modifying
    @Query("UPDATE CartItem ci SET ci.isDeleted = true, ci.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE ci.productId IN (SELECT p.id FROM Product p WHERE p.status != 'ACTIVE') " +
            "AND ci.isDeleted = false")
    int deleteCartItemsForInactiveProducts();

    @Modifying
    @Query("UPDATE CartItem ci SET ci.isDeleted = true, ci.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE ci.productSizeId IN (SELECT ps.id FROM ProductSize ps WHERE ps.isDeleted = true) " +
            "AND ci.isDeleted = false")
    int deleteCartItemsForDeletedProductSizes();

    @Modifying
    @Query("UPDATE CartItem ci SET ci.isDeleted = true, ci.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE ci.createdAt < :cutoffDate AND ci.isDeleted = false")
    int deleteOldCartItems(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.isDeleted = false")
    long countActiveCartItems();

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.createdAt < :cutoffDate AND ci.isDeleted = false")
    long countOldCartItems(@Param("cutoffDate") LocalDateTime cutoffDate);
}