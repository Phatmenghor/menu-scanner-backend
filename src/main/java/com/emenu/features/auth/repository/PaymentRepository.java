package com.emenu.features.auth.repository;

import com.emenu.enums.PaymentStatus;
import com.emenu.features.auth.models.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {
    
    // Basic CRUD operations
    Optional<Payment> findByIdAndIsDeletedFalse(UUID id);
    
    List<Payment> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    Page<Payment> findByBusinessIdAndIsDeletedFalse(UUID businessId, Pageable pageable);
    
    List<Payment> findBySubscriptionIdAndIsDeletedFalse(UUID subscriptionId);
    
    Optional<Payment> findByReferenceNumberAndIsDeletedFalse(String referenceNumber);
    
    boolean existsByReferenceNumberAndIsDeletedFalse(String referenceNumber);
    
    // Status-based queries
    Page<Payment> findByStatusAndIsDeletedFalse(PaymentStatus status, Pageable pageable);
    
    List<Payment> findByStatusInAndIsDeletedFalse(List<PaymentStatus> statuses);
    
    @Query("SELECT p FROM Payment p WHERE p.businessId = :businessId AND p.status = :status AND p.isDeleted = false")
    Page<Payment> findByBusinessIdAndStatusAndIsDeletedFalse(
        @Param("businessId") UUID businessId,
        @Param("status") PaymentStatus status, 
        Pageable pageable
    );
    
    // Financial queries
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.businessId = :businessId AND p.status = 'COMPLETED' AND p.isDeleted = false")
    BigDecimal getTotalPaidAmountByBusiness(@Param("businessId") UUID businessId);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :start AND :end AND p.isDeleted = false")
    BigDecimal getTotalRevenueInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT SUM(p.amountKhr) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :start AND :end AND p.isDeleted = false")
    BigDecimal getTotalRevenueKhrInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Count queries
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.planId = :planId AND p.status = 'COMPLETED' AND p.isDeleted = false")
    long countCompletedPaymentsByPlan(@Param("planId") UUID planId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.isDeleted = false")
    long countByStatus(@Param("status") PaymentStatus status);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.businessId = :businessId AND p.status = :status AND p.isDeleted = false")
    long countByBusinessIdAndStatus(@Param("businessId") UUID businessId, @Param("status") PaymentStatus status);
    
    // Overdue payments
    @Query("SELECT p FROM Payment p WHERE p.dueDate < :now AND p.status = 'PENDING' AND p.isDeleted = false")
    List<Payment> findOverduePayments(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.dueDate < :now AND p.status = 'PENDING' AND p.isDeleted = false")
    long countOverduePayments(@Param("now") LocalDateTime now);
    
    // Plan-related queries
    @Query("SELECT p FROM Payment p WHERE p.planId = :planId AND p.isDeleted = false")
    List<Payment> findByPlanId(@Param("planId") UUID planId);
    
    // Payment method statistics
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.isDeleted = false GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodStatistics();
    
    // Average payment amount
    @Query("SELECT AVG(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.isDeleted = false")
    Double getAveragePaymentAmount();
    
    // Recent payments
    @Query("SELECT p FROM Payment p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Payment> findRecentPayments(Pageable pageable);
    
    // Payments by date range
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :start AND :end AND p.isDeleted = false")
    List<Payment> findPaymentsByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Business payment summary
    @Query("SELECT " +
           "COUNT(p) as totalPayments, " +
           "SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END) as totalRevenue, " +
           "SUM(CASE WHEN p.status = 'PENDING' THEN p.amount ELSE 0 END) as pendingAmount " +
           "FROM Payment p WHERE p.businessId = :businessId AND p.isDeleted = false")
    Object[] getBusinessPaymentSummary(@Param("businessId") UUID businessId);
}