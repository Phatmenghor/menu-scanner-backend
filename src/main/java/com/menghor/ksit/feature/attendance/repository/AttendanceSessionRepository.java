package com.menghor.ksit.feature.attendance.repository;

import com.menghor.ksit.feature.attendance.models.AttendanceSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSessionEntity, Long>, JpaSpecificationExecutor<AttendanceSessionEntity> {
    List<AttendanceSessionEntity> findByScheduleIdOrderBySessionDateDesc(Long scheduleId);
    Optional<AttendanceSessionEntity> findByQrCode(String qrCode);
    
    @Query("SELECT a FROM AttendanceSessionEntity a WHERE a.teacher.id = :teacherId ORDER BY a.sessionDate DESC")
    List<AttendanceSessionEntity> findByTeacherId(Long teacherId);
    
    @Query("SELECT a FROM AttendanceSessionEntity a WHERE a.schedule.classes.id = :classId ORDER BY a.sessionDate DESC")
    List<AttendanceSessionEntity> findByClassId(Long classId);
    
    @Query("SELECT a FROM AttendanceSessionEntity a WHERE a.schedule.course.id = :courseId ORDER BY a.sessionDate DESC")
    List<AttendanceSessionEntity> findByCourseId(Long courseId);
    
    @Query("SELECT COUNT(a) FROM AttendanceSessionEntity a WHERE a.schedule.id = :scheduleId")
    Long countByScheduleId(Long scheduleId);
    
    @Query("SELECT COUNT(a) FROM AttendanceSessionEntity a WHERE a.schedule.id = :scheduleId AND a.sessionDate BETWEEN :startDate AND :endDate")
    Long countByScheduleIdAndSessionDateBetween(Long scheduleId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT a FROM AttendanceSessionEntity a WHERE a.schedule.id = :scheduleId AND a.sessionDate BETWEEN :startTime AND :endTime AND a.isFinal = false")
    Optional<AttendanceSessionEntity> findActiveSessionByScheduleAndTimeRange(Long scheduleId, LocalDateTime startTime, LocalDateTime endTime);
}