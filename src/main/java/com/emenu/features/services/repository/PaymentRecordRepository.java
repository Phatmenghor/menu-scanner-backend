package com.emenu.features.services.repository;

import com.emenu.features.services.domain.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, UUID>, JpaSpecificationExecutor<PaymentRecord> {
    Optional<PaymentRecord> findByIdAndIsDeletedFalse(UUID id);
}