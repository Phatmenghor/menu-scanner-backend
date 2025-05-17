package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.enumations.AttendanceType;
import com.menghor.ksit.feature.attendance.dto.request.AttendanceSessionRequest;
import com.menghor.ksit.feature.attendance.dto.request.QrAttendanceRequest;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceSessionDto;
import com.menghor.ksit.feature.attendance.dto.response.QrResponse;
import com.menghor.ksit.feature.attendance.mapper.AttendanceMapper;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import com.menghor.ksit.feature.attendance.models.AttendanceSessionEntity;
import com.menghor.ksit.feature.attendance.repository.AttendanceRepository;
import com.menghor.ksit.feature.attendance.repository.AttendanceSessionRepository;
import com.menghor.ksit.feature.attendance.service.AttendanceSessionService;
import com.menghor.ksit.feature.attendance.specification.AttendanceSessionSpecification;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceSessionServiceImpl implements AttendanceSessionService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    public AttendanceSessionDto findById(Long id) {
        AttendanceSessionEntity session = sessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attendance session not found with id: " + id));
        return attendanceMapper.toDto(session);
    }

    @Override
    public Page<AttendanceSessionDto> findAll(Long teacherId, Long scheduleId, Long classId, Long courseId,
                                              Boolean isFinal, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Specification<AttendanceSessionEntity> spec = Specification.where(AttendanceSessionSpecification.hasTeacherId(teacherId))
                .and(AttendanceSessionSpecification.hasScheduleId(scheduleId))
                .and(AttendanceSessionSpecification.hasClassId(classId))
                .and(AttendanceSessionSpecification.hasCourseId(courseId))
                .and(AttendanceSessionSpecification.isFinal(isFinal))
                .and(AttendanceSessionSpecification.sessionDateBetween(startDate, endDate));

        return sessionRepository.findAll(spec, pageable)
                .map(attendanceMapper::toDto);
    }

    @Override
    public List<AttendanceSessionDto> findByScheduleId(Long scheduleId) {
        return sessionRepository.findByScheduleIdOrderBySessionDateDesc(scheduleId)
                .stream()
                .map(attendanceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceSessionDto generateAttendanceSession(AttendanceSessionRequest request, Long teacherId) {
        // Validate the schedule exists
        ScheduleEntity schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found with id: " + request.getScheduleId()));
        
        // Verify teacher is assigned to this schedule
        if (!schedule.getUser().getId().equals(teacherId)) {
            throw new IllegalStateException("Teacher is not assigned to this schedule");
        }
        
        // Check if there's already an active session for this schedule
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusHours(1); // Allow for some flexibility in time
        LocalDateTime endTime = now.plusHours(1);
        
        Optional<AttendanceSessionEntity> existingSession = sessionRepository
                .findActiveSessionByScheduleAndTimeRange(request.getScheduleId(), startTime, endTime);
        
        if (existingSession.isPresent()) {
            return attendanceMapper.toDto(existingSession.get());
        }
        
        // Create new attendance session
        AttendanceSessionEntity session = new AttendanceSessionEntity();
        session.setSessionDate(now);
        session.setSchedule(schedule);
        session.setTeacher(userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId)));
        
        // Save session first to generate ID
        AttendanceSessionEntity savedSession = sessionRepository.save(session);
        
        // Create attendance records for all students in the class
        ClassEntity classEntity = schedule.getClasses();
        List<UserEntity> students = userRepository.findByClassesId(classEntity.getId());
        
        List<AttendanceEntity> attendances = new ArrayList<>();
        for (UserEntity student : students) {
            AttendanceEntity attendance = new AttendanceEntity();
            attendance.setStudent(student);
            attendance.setAttendanceSession(savedSession);
            attendance.setRecordedTime(now);
            attendances.add(attendance);
        }
        
        attendanceRepository.saveAll(attendances);
        savedSession.setAttendances(attendances);
        
        return attendanceMapper.toDto(savedSession);
    }

    @Override
    public QrResponse generateQrCode(Long sessionId) {
        AttendanceSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Attendance session not found with id: " + sessionId));
        
        if (session.isFinal()) {
            throw new IllegalStateException("Cannot generate QR code for finalized session");
        }
        
        // If QR code has expired, generate a new one
        if (LocalDateTime.now().isAfter(session.getQrExpiryTime())) {
            session.generateQrCode();
            sessionRepository.save(session);
        }
        
        return QrResponse.builder()
                .qrCode(session.getQrCode())
                .expiryTime(session.getQrExpiryTime().toString())
                .build();
    }

    @Override
    @Transactional
    public AttendanceSessionDto markAttendanceByQr(QrAttendanceRequest request) {
        // Find session by QR code
        AttendanceSessionEntity session = sessionRepository.findByQrCode(request.getQrCode())
                .orElseThrow(() -> new EntityNotFoundException("Invalid or expired QR code"));
        
        // Check if QR code has expired
        if (LocalDateTime.now().isAfter(session.getQrExpiryTime())) {
            throw new IllegalStateException("QR code has expired");
        }
        
        // Check if session is finalized
        if (session.isFinal()) {
            throw new IllegalStateException("Attendance session is already finalized");
        }
        
        // Find student's attendance record
        AttendanceEntity attendance = attendanceRepository
                .findByAttendanceSessionIdAndStudentId(session.getId(), request.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found in this attendance session"));
        
        // Mark as present
        attendance.setStatus(AttendanceStatus.PRESENT);
        
        // Check if student is late (more than 10 minutes after session start)
        if (LocalDateTime.now().isAfter(session.getSessionDate().plusMinutes(10))) {
            attendance.setAttendanceType(AttendanceType.LATE);
        }
        
        attendance.setRecordedTime(LocalDateTime.now());
        attendanceRepository.save(attendance);
        
        return attendanceMapper.toDto(session);
    }

    @Override
    @Transactional
    public AttendanceSessionDto finalizeAttendanceSession(Long sessionId) {
        AttendanceSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Attendance session not found with id: " + sessionId));
        
        if (session.isFinal()) {
            return attendanceMapper.toDto(session);
        }
        
        // Finalize the session
        session.setFinal(true);
        
        // Finalize all attendance records
        List<AttendanceEntity> attendances = attendanceRepository.findByAttendanceSessionId(sessionId);
        for (AttendanceEntity attendance : attendances) {
            // If status is still null, mark as ABSENT
            if (attendance.getStatus() == null) {
                attendance.setStatus(AttendanceStatus.ABSENT);
            }
            attendance.setFinal(true);
        }
        
        attendanceRepository.saveAll(attendances);
        sessionRepository.save(session);
        
        return attendanceMapper.toDto(session);
    }
}