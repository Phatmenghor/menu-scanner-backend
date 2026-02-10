package com.emenu.features.order.repository;

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
     * Finds a non-deleted order by ID with items, products, sizes, business, and customer eagerly fetched
     */
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.items oi " +
           "LEFT JOIN FETCH oi.product p " +
           "LEFT JOIN FETCH oi.productSize ps " +
           "LEFT JOIN FETCH o.business " +
           "LEFT JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.orderProcessStatus " +
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
     * Finds non-deleted orders by business ID and process status, ordered by creation date descending
     */
    @Query("SELECT o FROM Order o WHERE o.businessId = :businessId AND o.orderProcessStatusId = :orderProcessStatusId AND o.isDeleted = false ORDER BY o.createdAt DESC")
    List<Order> findByBusinessIdAndOrderProcessStatusIdOrderByCreatedAtDesc(@Param("businessId") UUID businessId, @Param("orderProcessStatusId") UUID orderProcessStatusId);

    /**
     * Checks if an order exists with the given order number
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Counts non-deleted orders by business ID and process status
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.businessId = :businessId AND o.orderProcessStatusId = :orderProcessStatusId AND o.isDeleted = false")
    long countByBusinessIdAndOrderProcessStatusId(@Param("businessId") UUID businessId, @Param("orderProcessStatusId") UUID orderProcessStatusId);
}
