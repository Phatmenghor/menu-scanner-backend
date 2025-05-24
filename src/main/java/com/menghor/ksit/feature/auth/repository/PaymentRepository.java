package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long>, JpaSpecificationExecutor<PaymentEntity> {

    List<PaymentEntity> findByUserIdAndStatusNot(Long userId, Status status);

}