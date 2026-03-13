package com.emenu.features.order.repository;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.features.order.models.OrderPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, UUID> {

    /**
     * Finds a non-deleted business order payment by ID
     */
    Optional<OrderPayment> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds a non-deleted business order payment by order ID
     */
    Optional<OrderPayment> findByOrderIdAndIsDeletedFalse(UUID orderId);

    /**
     * Finds a non-deleted business order payment by ID with business, order, and customer details eagerly fetched
     */
    @Query("SELECT bop FROM OrderPayment bop " +
           "LEFT JOIN FETCH bop.business " +
           "LEFT JOIN FETCH bop.order o " +
           "LEFT JOIN FETCH o.customer " +
           "WHERE bop.id = :id AND bop.isDeleted = false")
    Optional<OrderPayment> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Finds all non-deleted business order payments by business ID, ordered by creation date descending
     */
    @Query("SELECT bop FROM OrderPayment bop WHERE bop.businessId = :businessId AND bop.isDeleted = false ORDER BY bop.createdAt DESC")
    List<OrderPayment> findByBusinessIdOrderByCreatedAtDesc(@Param("businessId") UUID businessId);

    /**
     * Checks if a non-deleted business order payment exists with the given payment reference
     */
    boolean existsByPaymentReferenceAndIsDeletedFalse(String paymentReference);

    /**
     * Calculates total revenue for a business from completed payments
     */
    @Query("SELECT SUM(bop.totalAmount) FROM OrderPayment bop WHERE bop.businessId = :businessId AND bop.status = 'COMPLETED' AND bop.isDeleted = false")
    BigDecimal getTotalRevenue(@Param("businessId") UUID businessId);

    /**
     * Calculates revenue for a business within a date range from completed payments
     */
    @Query("SELECT SUM(bop.totalAmount) FROM OrderPayment bop WHERE bop.businessId = :businessId AND bop.status = 'COMPLETED' AND bop.createdAt >= :fromDate AND bop.createdAt <= :toDate AND bop.isDeleted = false")
    BigDecimal getRevenueByDateRange(@Param("businessId") UUID businessId, @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    /**
     * Find all business order payments with dynamic filtering
     */
    @Query("SELECT bop FROM OrderPayment bop " +
           "WHERE bop.isDeleted = false " +
           "AND (:businessId IS NULL OR bop.businessId = :businessId) " +
           "AND (:statuses IS NULL OR bop.status IN :statuses) " +
           "AND (:paymentMethod IS NULL OR bop.paymentMethod = :paymentMethod) " +
           "AND (:customerPaymentMethod IS NULL OR bop.customerPaymentMethod = :customerPaymentMethod) " +
           "AND (:createdFrom IS NULL OR bop.createdAt >= :createdFrom) " +
           "AND (:createdTo IS NULL OR bop.createdAt <= :createdTo) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(bop.paymentReference) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<OrderPayment> findAllWithFilters(
        @Param("businessId") UUID businessId,
        @Param("statuses") List<PaymentStatus> statuses,
        @Param("paymentMethod") PaymentMethod paymentMethod,
        @Param("customerPaymentMethod") String customerPaymentMethod,
        @Param("createdFrom") LocalDateTime createdFrom,
        @Param("createdTo") LocalDateTime createdTo,
        @Param("search") String search,
        Pageable pageable
    );
}
