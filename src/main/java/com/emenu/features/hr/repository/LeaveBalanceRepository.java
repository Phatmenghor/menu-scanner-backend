package com.emenu.features.hr.repository;

import com.emenu.features.hr.models.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {
    Optional<LeaveBalance> findByIdAndIsDeletedFalse(UUID id);
    List<LeaveBalance> findByUserIdAndIsDeletedFalse(UUID userId);
    List<LeaveBalance> findByUserIdAndYearAndIsDeletedFalse(UUID userId, Integer year);
    Optional<LeaveBalance> findByUserIdAndPolicyIdAndYearAndIsDeletedFalse(
        UUID userId, UUID policyId, Integer year);
}
