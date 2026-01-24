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

    /**
     * Finds a non-deleted order by ID with items, products, sizes, delivery details, business, and customer eagerly fetched
     */
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

    /**
     * Finds all non-deleted orders by customer ID, ordered by creation date descending
     */
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") UUID customerId);

    /**
     * Finds all non-deleted orders by business ID, ordered by creation date descending
     */
    @Query("SELECT o FROM Order o WHERE o.businessId = :businessId AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findByBusinessIdOrderByCreatedAtDesc(@Param("businessId") UUID businessId);

    /**
     * Finds non-deleted orders by business ID and status, ordered by creation date descending
     */
    @Query("SELECT o FROM Order o WHERE o.businessId = :businessId AND o.status = :status AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findByBusinessIdAndStatusOrderByCreatedAtDesc(@Param("businessId") UUID businessId, @Param("status") OrderStatus status);

    /**
     * Finds non-deleted orders by guest phone number, ordered by creation date descending
     */
    @Query("SELECT o FROM Order o WHERE o.guestPhone = :phone AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findByGuestPhoneOrderByCreatedAtDesc(@Param("phone") String phone);

    /**
     * Finds non-deleted POS orders by business ID, ordered by creation date descending
     */
    @Query("SELECT o FROM Order o WHERE o.isPosOrder = true AND o.businessId = :businessId AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findPosOrdersByBusinessId(@Param("businessId") UUID businessId);

    /**
     * Finds non-deleted guest orders by business ID, ordered by creation date descending
     */
    @Query("SELECT o FROM Order o WHERE o.isGuestOrder = true AND o.businessId = :businessId AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findGuestOrdersByBusinessId(@Param("businessId") UUID businessId);

    /**
     * Checks if an order exists with the given order number
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Counts non-deleted orders by business ID and status
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.businessId = :businessId AND o.status = :status AND o.isDeleted = false")
    long countByBusinessIdAndStatus(@Param("businessId") UUID businessId, @Param("status") OrderStatus status);

    /**
     * Counts non-deleted POS orders by business ID
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.businessId = :businessId AND o.isPosOrder = true AND o.isDeleted = false")
    long countPosOrdersByBusinessId(@Param("businessId") UUID businessId);

    /**
     * Counts non-deleted guest orders by business ID
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.businessId = :businessId AND o.isGuestOrder = true AND o.isDeleted = false")
    long countGuestOrdersByBusinessId(@Param("businessId") UUID businessId);
}