package com.emenu.features.order.repository;

import com.emenu.features.order.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    
    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.items ci " +
           "LEFT JOIN FETCH ci.product p " +
           "LEFT JOIN FETCH ci.productSize ps " +
           "LEFT JOIN FETCH c.business " +
           "WHERE c.userId = :userId AND c.businessId = :businessId AND c.isDeleted = false")
    Optional<Cart> findByUserIdAndBusinessIdWithItems(@Param("userId") UUID userId, @Param("businessId") UUID businessId);
    
    Optional<Cart> findByUserIdAndBusinessIdAndIsDeletedFalse(UUID userId, UUID businessId);
}
