package com.emenu.features.order.repository;

import com.emenu.features.order.models.BusinessOrderPayment;
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
public interface BusinessOrderPaymentRepository extends JpaRepository<BusinessOrderPayment, UUID>, JpaSpecificationExecutor<BusinessOrderPayment> {
    
    Optional<BusinessOrderPayment> findByIdAndIsDeletedFalse(UUID id);
    
    Optional<BusinessOrderPayment> findByOrderIdAndIsDeletedFalse(UUID orderId);
    
    @Query("SELECT bop FROM BusinessOrderPayment bop " +
           "LEFT JOIN FETCH bop.business " +
           "LEFT JOIN FETCH bop.order o " +
           "LEFT JOIN FETCH o.customer " +
           "WHERE bop.id = :id AND bop.isDeleted = false")
    Optional<BusinessOrderPayment> findByIdWithDetails(@Param("id") UUID id);
    
    @Query("SELECT bop FROM BusinessOrderPayment bop WHERE bop.businessId = :businessId AND bop.isDeleted = false ORDER BY bop.createdAt DESC")
    List<BusinessOrderPayment> findByBusinessIdOrderByCreatedAtDesc(@Param("businessId") UUID businessId);
    
    boolean existsByPaymentReferenceAndIsDeletedFalse(String paymentReference);
    
    // Statistics queries
    @Query("SELECT SUM(bop.amount) FROM BusinessOrderPayment bop WHERE bop.businessId = :businessId AND bop.status = 'COMPLETED' AND bop.isDeleted = false")
    BigDecimal getTotalRevenue(@Param("businessId") UUID businessId);
    
    @Query("SELECT SUM(bop.amount) FROM BusinessOrderPayment bop WHERE bop.businessId = :businessId AND bop.status = 'COMPLETED' AND bop.createdAt >= :fromDate AND bop.createdAt <= :toDate AND bop.isDeleted = false")
    BigDecimal getRevenueByDateRange(@Param("businessId") UUID businessId, @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
    
    @Query("SELECT COUNT(bop) FROM BusinessOrderPayment bop WHERE bop.businessId = :businessId AND bop.order.isPosOrder = true AND bop.isDeleted = false")
    long countPosPayments(@Param("businessId") UUID businessId);
    
    @Query("SELECT COUNT(bop) FROM BusinessOrderPayment bop WHERE bop.businessId = :businessId AND bop.order.isGuestOrder = true AND bop.isDeleted = false")
    long countGuestPayments(@Param("businessId") UUID businessId);
}