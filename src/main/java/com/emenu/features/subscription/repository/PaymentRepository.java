package com.emenu.features.subscription.repository;

import com.emenu.enums.PaymentStatus;
import com.emenu.features.subscription.models.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    Optional<Payment> findByIdAndIsDeletedFalse(UUID id);
    List<Payment> findBySubscriptionIdAndIsDeletedFalse(UUID subscriptionId);
    Page<Payment> findBySubscriptionIdAndIsDeletedFalse(UUID subscriptionId, Pageable pageable);
    List<Payment> findByStatusAndIsDeletedFalse(PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.dueDate < :now AND p.status IN ('PENDING', 'PROCESSING') AND p.isDeleted = false")
    List<Payment> findOverduePayments(@Param("now") LocalDateTime now);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paidAt BETWEEN :start AND :end AND p.isDeleted = false")
    Double calculateRevenueForPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}