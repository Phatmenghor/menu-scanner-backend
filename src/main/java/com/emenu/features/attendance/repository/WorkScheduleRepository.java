package com.emenu.features.attendance.repository;

import com.emenu.features.attendance.models.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long>, JpaSpecificationExecutor<WorkSchedule> {

    List<WorkSchedule> findByUserId(Long userId);

    Optional<WorkSchedule> findByUserIdAndIsActive(Long userId, Boolean isActive);

    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.attendancePolicy.business.id = :businessId")
    List<WorkSchedule> findByBusinessId(Long businessId);
}
