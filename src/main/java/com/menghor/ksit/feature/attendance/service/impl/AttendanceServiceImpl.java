package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceDto;
import com.menghor.ksit.feature.attendance.dto.update.AttendanceUpdateRequest;
import com.menghor.ksit.feature.attendance.mapper.AttendanceMapper;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import com.menghor.ksit.feature.attendance.repository.AttendanceRepository;
import com.menghor.ksit.feature.attendance.repository.AttendanceSessionRepository;
import com.menghor.ksit.feature.attendance.service.AttendanceService;
import com.menghor.ksit.feature.attendance.specification.AttendanceSpecification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    public AttendanceDto findById(Long id) {
        return attendanceRepository.findById(id)
                .map(attendanceMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found with id: " + id));
    }

    @Override
    public Page<AttendanceDto> findAll(Long studentId, Long sessionId, AttendanceStatus status,
                                       LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Specification<AttendanceEntity> spec = Specification.where(AttendanceSpecification.hasStudentId(studentId))
                .and(AttendanceSpecification.hasSessionId(sessionId))
                .and(AttendanceSpecification.hasStatus(status))
                .and(AttendanceSpecification.recordedBetween(startDate, endDate));

        return attendanceRepository.findAll(spec, pageable)
                .map(attendanceMapper::toDto);
    }

    @Override
    public List<AttendanceDto> findBySessionId(Long sessionId) {
        return attendanceRepository.findByAttendanceSessionId(sessionId)
                .stream()
                .map(attendanceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceDto updateAttendance(AttendanceUpdateRequest request) {
        AttendanceEntity attendance = attendanceRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found with id: " + request.getId()));

        // Check if the attendance session is finalized
        if (attendance.getAttendanceSession().isFinal()) {
            throw new IllegalStateException("Cannot update attendance for a finalized session");
        }

        attendance.setStatus(request.getStatus());
        attendance.setAttendanceType(request.getAttendanceType());
        attendance.setComment(request.getComment());
        attendance.setRecordedTime(LocalDateTime.now());

        return attendanceMapper.toDto(attendanceRepository.save(attendance));
    }

    @Override
    public Double calculateAttendanceScore(Long studentId, Long scheduleId) {
        return calculateAttendanceScore(studentId, scheduleId, null, null);
    }

    @Override
    public Double calculateAttendanceScore(Long studentId, Long scheduleId, LocalDateTime startDate, LocalDateTime endDate) {
        // Count total sessions
        long totalSessions;
        if (startDate != null && endDate != null) {
            totalSessions = sessionRepository.countByScheduleIdAndSessionDateBetween(scheduleId, startDate, endDate);
        } else {
            totalSessions = sessionRepository.countByScheduleId(scheduleId);
        }
        
        if (totalSessions == 0) {
            return 100.0; // No sessions, perfect attendance
        }
        
        // Count attended sessions
        long attendedSessions = attendanceRepository.countByStudentIdAndScheduleIdAndStatus(
            studentId, scheduleId, AttendanceStatus.PRESENT
        );
        
        return (double) attendedSessions / totalSessions * 100;
    }
}