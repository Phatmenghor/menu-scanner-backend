package com.emenu.features.order.repository;

import com.emenu.features.order.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}