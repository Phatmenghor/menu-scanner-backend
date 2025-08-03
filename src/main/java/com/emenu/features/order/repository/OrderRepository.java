package com.emenu.features.order.repository;

import com.emenu.enums.order.OrderStatus;
import com.emenu.features.order.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.items oi " +
           "LEFT JOIN FETCH oi.product p " +
           "LEFT JOIN FETCH oi.productSize ps " +
           "LEFT JOIN FETCH o.deliveryAddress " +
           "LEFT JOIN FETCH o.deliveryOption " +
           "LEFT JOIN FETCH o.business " +
           "LEFT JOIN FETCH o.customer " +
           "WHERE o.id = :id AND o.isDeleted = false")
    Optional<Order> findByIdWithDetails(@Param("id") UUID id);
    
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") UUID customerId);
    
    @Query("SELECT o FROM Order o WHERE o.businessId = :businessId AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findByBusinessIdOrderByCreatedAtDesc(@Param("businessId") UUID businessId);
    
    @Query("SELECT o FROM Order o WHERE o.businessId = :businessId AND o.status = :status AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findByBusinessIdAndStatusOrderByCreatedAtDesc(@Param("businessId") UUID businessId, @Param("status") OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.guestPhone = :phone AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findByGuestPhoneOrderByCreatedAtDesc(@Param("phone") String phone);
    
    @Query("SELECT o FROM Order o WHERE o.isPosOrder = true AND o.businessId = :businessId AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findPosOrdersByBusinessId(@Param("businessId") UUID businessId);
    
    @Query("SELECT o FROM Order o WHERE o.isGuestOrder = true AND o.businessId = :businessId AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findGuestOrdersByBusinessId(@Param("businessId") UUID businessId);
    
    boolean existsByOrderNumber(String orderNumber);
    
    // Statistics queries
    @Query("SELECT COUNT(o) FROM Order o WHERE o.businessId = :businessId AND o.status = :status AND o.isDeleted = false")
    long countByBusinessIdAndStatus(@Param("businessId") UUID businessId, @Param("status") OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.businessId = :businessId AND o.isPosOrder = true AND o.isDeleted = false")
    long countPosOrdersByBusinessId(@Param("businessId") UUID businessId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.businessId = :businessId AND o.isGuestOrder = true AND o.isDeleted = false")
    long countGuestOrdersByBusinessId(@Param("businessId") UUID businessId);
}