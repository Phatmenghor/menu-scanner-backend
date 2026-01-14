package com.emenu.features.leave.repository;

import com.emenu.enums.leave.LeaveType;
import com.emenu.features.leave.models.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long>, JpaSpecificationExecutor<LeavePolicy> {

    List<LeavePolicy> findByBusinessId(Long businessId);

    Optional<LeavePolicy> findByIdAndBusinessId(Long id, Long businessId);

    List<LeavePolicy> findByBusinessIdAndIsActive(Long businessId, Boolean isActive);

    Optional<LeavePolicy> findByBusinessIdAndLeaveType(Long businessId, LeaveType leaveType);
}
