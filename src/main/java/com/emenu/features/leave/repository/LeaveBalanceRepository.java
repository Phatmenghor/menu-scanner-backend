package com.emenu.features.leave.repository;

import com.emenu.features.leave.models.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long>, JpaSpecificationExecutor<LeaveBalance> {

    List<LeaveBalance> findByUserIdAndYear(Long userId, Integer year);

    Optional<LeaveBalance> findByUserIdAndLeavePolicyIdAndYear(Long userId, Long leavePolicyId, Integer year);

    List<LeaveBalance> findByUserId(Long userId);
}
