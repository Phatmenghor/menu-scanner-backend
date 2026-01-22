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

    /**
     * Finds all non-deleted check-in records for a given attendance
     */
    List<AttendanceCheckIn> findByAttendanceIdAndIsDeletedFalse(UUID attendanceId);

    /**
     * Finds a non-deleted check-in record by attendance ID and check-in type
     */
    Optional<AttendanceCheckIn> findByAttendanceIdAndCheckInTypeAndIsDeletedFalse(
        UUID attendanceId, CheckInType checkInType);
}
