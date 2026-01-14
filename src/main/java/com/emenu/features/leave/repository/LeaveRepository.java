package com.emenu.features.leave.repository;

import com.emenu.enums.leave.LeaveStatus;
import com.emenu.features.leave.models.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long>, JpaSpecificationExecutor<Leave> {

    List<Leave> findByUserId(Long userId);

    List<Leave> findByUserIdAndStatus(Long userId, LeaveStatus status);

    @Query("SELECT l FROM Leave l WHERE l.leavePolicy.business.id = :businessId")
    List<Leave> findByBusinessId(Long businessId);

    @Query("SELECT l FROM Leave l WHERE l.userId = :userId AND l.startDate <= :endDate AND l.endDate >= :startDate")
    List<Leave> findOverlappingLeaves(Long userId, LocalDate startDate, LocalDate endDate);
}
