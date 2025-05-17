package com.menghor.ksit.feature.attendance.repository;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long>, JpaSpecificationExecutor<AttendanceEntity> {
    List<AttendanceEntity> findByAttendanceSessionId(Long sessionId);
    
    Optional<AttendanceEntity> findByAttendanceSessionIdAndStudentId(Long sessionId, Long studentId);
    
    @Query("SELECT COUNT(a) FROM AttendanceEntity a WHERE a.student.id = :studentId AND a.attendanceSession.schedule.id = :scheduleId AND a.status = :status")
    Long countByStudentIdAndScheduleIdAndStatus(Long studentId, Long scheduleId, AttendanceStatus status);
    
    @Query("SELECT a FROM AttendanceEntity a WHERE a.student.id = :studentId ORDER BY a.attendanceSession.sessionDate DESC")
    List<AttendanceEntity> findByStudentId(Long studentId);
    
    @Query("SELECT a FROM AttendanceEntity a WHERE a.student.id = :studentId AND a.attendanceSession.schedule.id = :scheduleId ORDER BY a.attendanceSession.sessionDate DESC")
    List<AttendanceEntity> findByStudentIdAndScheduleId(Long studentId, Long scheduleId);
}