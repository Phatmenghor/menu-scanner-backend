package com.emenu.features.auth.repository;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.features.auth.models.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {
    
    // Basic CRUD operations
    Optional<Payment> findByIdAndIsDeletedFalse(UUID id);
    
    Page<Payment> findByIsDeletedFalse(Pageable pageable);
    
    List<Payment> findByIsDeletedFalse();
    
    // Business specific operations
    List<Payment> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    Page<Payment> findByBusinessIdAndIsDeletedFalse(UUID businessId, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.businessId = :businessId AND p.isDeleted = false")
    long countByBusinessIdAndIsDeletedFalse(@Param("businessId") UUID businessId);
    
    // Reference number operations
    Optional<Payment> findByReferenceNumberAndIsDeletedFalse(String referenceNumber);
    
    boolean existsByReferenceNumberAndIsDeletedFalse(String referenceNumber);
    
    // Status based operations
    List<Payment> findByStatusAndIsDeletedFalse(PaymentStatus status);
    
    Page<Payment> findByStatusAndIsDeletedFalse(PaymentStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.isDeleted = false")
    long countByStatusAndIsDeletedFalse(@Param("status") PaymentStatus status);
    
    // Plan specific operations
    List<Payment> findByPlanIdAndIsDeletedFalse(UUID planId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.planId = :planId AND p.status = 'COMPLETED' AND p.isDeleted = false")
    long countCompletedPaymentsByPlan(@Param("planId") UUID planId);
    
    // Payment method specific
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :paymentMethod AND p.isDeleted = false")
    List<Payment> findByPaymentMethodAndIsDeletedFalse(@Param("paymentMethod") PaymentMethod paymentMethod);
    
    // Search operations
    @Query("SELECT p FROM Payment p WHERE p.isDeleted = false AND " +
           "(LOWER(p.referenceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.notes) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Payment> findBySearchAndIsDeletedFalse(@Param("search") String search, Pageable pageable);
}