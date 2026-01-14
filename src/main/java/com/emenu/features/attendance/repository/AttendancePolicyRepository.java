package com.emenu.features.attendance.repository;

import com.emenu.features.attendance.models.AttendancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendancePolicyRepository extends JpaRepository<AttendancePolicy, Long>, JpaSpecificationExecutor<AttendancePolicy> {

    List<AttendancePolicy> findByBusinessId(Long businessId);

    Optional<AttendancePolicy> findByIdAndBusinessId(Long id, Long businessId);

    List<AttendancePolicy> findByBusinessIdAndIsActive(Long businessId, Boolean isActive);
}
