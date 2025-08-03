package com.emenu.features.order.repository;

import com.emenu.enums.order.OrderStatus;
import com.emenu.features.order.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.items " +
           "LEFT JOIN FETCH o.deliveryAddress " +
           "LEFT JOIN FETCH o.deliveryOption " +
           "WHERE o.id = :id AND o.isDeleted = false")
    Optional<Order> findByIdWithDetails(@Param("id") UUID id);
    
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") UUID customerId);
    
    @Query("SELECT o FROM Order o WHERE o.businessId = :businessId AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findByBusinessIdOrderByCreatedAtDesc(@Param("businessId") UUID businessId);
    
    @Query("SELECT o FROM Order o WHERE o.businessId = :businessId AND o.status = :status AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findByBusinessIdAndStatusOrderByCreatedAtDesc(@Param("businessId") UUID businessId, @Param("status") OrderStatus status);
    
    boolean existsByOrderNumber(String orderNumber);
}