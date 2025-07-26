package com.emenu.features.payment.repository;

import com.emenu.features.payment.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {
    Optional<Payment> findByIdAndIsDeletedFalse(UUID id);
    
    boolean existsByReferenceNumberAndIsDeletedFalse(String referenceNumber);

    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.business " +
            "LEFT JOIN FETCH p.plan " +
            "LEFT JOIN FETCH p.subscription " +
            "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Payment> findByIdWithRelationships(@Param("id") UUID id);
}