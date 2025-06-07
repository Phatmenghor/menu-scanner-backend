package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.AttendanceFinalizationStatus;
import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.enumations.AttendanceType;
import com.menghor.ksit.enumations.Status;
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
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
    @Transactional
    public AttendanceSessionDto generateAttendanceSession(AttendanceSessionRequest request) {
        log.info("Generating attendance session for schedule ID: {}", request.getScheduleId());

        // Validate schedule exists
        ScheduleEntity schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found with id: " + request.getScheduleId()));


        // Get current date/time
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // Check if an attendance session already exists for this schedule today
        List<AttendanceSessionEntity> todaySessions = findTodaySessionsForSchedule(schedule.getId(), today);

        // Check if there's a DRAFT session for today
        Optional<AttendanceSessionEntity> draftSession = todaySessions.stream()
                .filter(session -> session.getFinalizationStatus() == AttendanceFinalizationStatus.DRAFT)
                .findFirst();

        // If a draft session exists for today, return it (don't create a new one)
        if (draftSession.isPresent()) {
            log.info("Draft attendance session already exists for today, returning existing session ID: {}",
                    draftSession.get().getId());
            // Sorting will be handled by the mapper
            return attendanceMapper.toDto(draftSession.get());
        }

        // Create new attendance session
        AttendanceSessionEntity session = new AttendanceSessionEntity();
        session.setSessionDate(now);
        session.setSchedule(schedule);
        session.setStatus(Status.ACTIVE);
        session.setFinalizationStatus(AttendanceFinalizationStatus.DRAFT);
        session.setTeacher(userRepository.findById(schedule.getUser().getId())
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + schedule.getUser().getId())));

        // Generate QR code
        session.setQrCode(UUID.randomUUID().toString());
        session.setQrExpiryTime(now.plusMinutes(15)); // QR code valid for 15 minutes

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
            attendance.setAttendanceType(AttendanceType.NONE); // No type yet
            attendance.setStatus(AttendanceStatus.ABSENT); // Not marked yet
            attendance.setRecordedTime(now); // Not recorded yet
            attendance.setFinalizationStatus(AttendanceFinalizationStatus.DRAFT);
            attendances.add(attendance);
        }

        // Save all attendance records
        attendanceRepository.saveAll(attendances);

        log.info("Created new attendance session with ID: {} with {} students", savedSession.getId(), attendances.size());

        // The sorting will be handled automatically in the mapper
        return attendanceMapper.toDto(savedSession);
    }

    /**
     * Find all attendance sessions for a schedule on a specific date
     */
    private List<AttendanceSessionEntity> findTodaySessionsForSchedule(Long scheduleId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return sessionRepository.findByScheduleIdAndSessionDateBetween(scheduleId, startOfDay, endOfDay);
    }

    @Override
    @Transactional
    public QrResponse regenerateQrCode(Long sessionId) {
        AttendanceSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Attendance session not found with id: " + sessionId));

        // Only allow regeneration if session is in DRAFT status
        if (session.getFinalizationStatus() != AttendanceFinalizationStatus.DRAFT) {
            throw new IllegalStateException("Cannot regenerate QR code for finalized session");
        }

        // Generate new QR code and reset expiry time (15 minutes from now)
        LocalDateTime now = LocalDateTime.now();
        session.setQrCode(UUID.randomUUID().toString());
        session.setQrExpiryTime(now.plusMinutes(15));

        session = sessionRepository.save(session);

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

        // Find student's attendance record
        AttendanceEntity attendance = attendanceRepository
                .findByAttendanceSessionIdAndStudentId(session.getId(), request.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found in this attendance session"));

        // Mark as present
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setRecordedTime(LocalDateTime.now());
        attendanceRepository.save(attendance);

        return attendanceMapper.toDto(session);
    }

    @Override
    @Transactional
    public AttendanceSessionDto finalizeAttendanceSession(Long sessionId) {
        AttendanceSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Attendance session not found with id: " + sessionId));

        if (session.getFinalizationStatus() == AttendanceFinalizationStatus.FINAL) {
            return attendanceMapper.toDto(session);
        }

        // Set status to final
        session.setFinalizationStatus(AttendanceFinalizationStatus.FINAL);

        // Process all attendance records
        List<AttendanceEntity> attendances = attendanceRepository.findByAttendanceSessionId(sessionId);
        for (AttendanceEntity attendance : attendances) {
            if (attendance.getStatus() == null) {
                attendance.setStatus(AttendanceStatus.ABSENT);
            }
            attendance.setFinalizationStatus(AttendanceFinalizationStatus.FINAL);
        }

        // Save both the attendances and session
        attendanceRepository.saveAll(attendances);
        session = sessionRepository.save(session);

        return attendanceMapper.toDto(session);
    }
}