package com.emenu.features.hr.repository;

import com.emenu.enums.hr.CheckInType;
import com.emenu.features.hr.models.AttendanceCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceCheckInRepository extends JpaRepository<AttendanceCheckIn, UUID> {
    List<AttendanceCheckIn> findByAttendanceIdAndIsDeletedFalse(UUID attendanceId);
    Optional<AttendanceCheckIn> findByAttendanceIdAndCheckInTypeAndIsDeletedFalse(
        UUID attendanceId, CheckInType checkInType);
}
