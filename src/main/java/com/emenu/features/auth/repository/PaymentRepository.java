package com.emenu.features.auth.repository;

import com.emenu.enums.PaymentStatus;
import com.emenu.enums.SubscriptionPlan;
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
    
    Optional<Payment> findByIdAndIsDeletedFalse(UUID id);
    
    List<Payment> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    Page<Payment> findByBusinessIdAndIsDeletedFalse(UUID businessId, Pageable pageable);
    
    List<Payment> findBySubscriptionIdAndIsDeletedFalse(UUID subscriptionId);
    
    Page<Payment> findByStatusAndIsDeletedFalse(PaymentStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.businessId = :businessId AND p.status = :status AND p.isDeleted = false")
    Page<Payment> findByBusinessIdAndStatusAndIsDeletedFalse(@Param("businessId") UUID businessId,
                                                             @Param("status") PaymentStatus status, 
                                                             Pageable pageable);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.businessId = :businessId AND p.status = 'COMPLETED' AND p.isDeleted = false")
    BigDecimal getTotalPaidAmountByBusiness(@Param("businessId") UUID businessId);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :start AND :end AND p.isDeleted = false")
    BigDecimal getTotalRevenueInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.subscriptionPlan = :plan AND p.status = 'COMPLETED' AND p.isDeleted = false")
    long countCompletedPaymentsByPlan(@Param("plan") SubscriptionPlan plan);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.isDeleted = false")
    long countByStatus(@Param("status") PaymentStatus status);
}