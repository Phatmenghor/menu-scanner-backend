package com.emenu.features.attendance.repository;

import com.emenu.features.attendance.models.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    Optional<Attendance> findByUserIdAndAttendanceDate(Long userId, LocalDate date);

    List<Attendance> findByUserIdAndAttendanceDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT a FROM Attendance a WHERE a.workSchedule.attendancePolicy.business.id = :businessId AND a.attendanceDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByBusinessIdAndDateRange(Long businessId, LocalDate startDate, LocalDate endDate);
}
