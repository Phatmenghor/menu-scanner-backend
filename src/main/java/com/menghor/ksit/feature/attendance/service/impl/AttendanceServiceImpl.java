package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.dto.request.AttendanceUpdateRequest;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceDto;
import com.menghor.ksit.feature.attendance.mapper.AttendanceMapper;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import com.menghor.ksit.feature.attendance.models.AttendanceSessionEntity;
import com.menghor.ksit.feature.attendance.repository.AttendanceRepository;
import com.menghor.ksit.feature.attendance.repository.AttendanceSessionRepository;
import com.menghor.ksit.feature.attendance.service.AttendanceService;
import com.menghor.ksit.feature.attendance.specification.AttendanceSpecification;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        attendance.setComment(request.getComment());
        attendance.setRecordedTime(LocalDateTime.now());

        return attendanceMapper.toDto(attendanceRepository.save(attendance));
    }

    @Override
    public Double calculateAttendanceScore(Long studentId, Long scheduleId) {
        return calculateSingleStudentScore(studentId, scheduleId, null, null);
    }

    @Override
    public AttendanceScoreDto calculateForStudent(Long studentId, Long scheduleId,
                                                 LocalDateTime startDate, LocalDateTime endDate) {
        return calculateSingleStudentScore(studentId, scheduleId, startDate, endDate);
    }

    @Override
    public List<AttendanceScoreDto> calculateForClass(Long classId, Long scheduleId) {
        return calculateClassScores(classId, scheduleId, null, null);
    }

    @Override
    public List<AttendanceScoreDto> calculateForClass(Long classId, Long scheduleId, 
                                                    LocalDateTime startDate, LocalDateTime endDate) {
        return calculateClassScores(classId, scheduleId, startDate, endDate);
    }

    @Override
    public Page<AttendanceScoreDto> calculateForCourse(Long courseId, Long semesterId, Pageable pageable) {
        // Get all schedules for this course and semester
        List<ScheduleEntity> schedules = scheduleRepository.findByCourseIdAndSemesterId(courseId, semesterId);
        
        if (schedules.isEmpty()) {
            return Page.empty(pageable);
        }
        
        List<AttendanceScoreDto> allScores = new ArrayList<>();
        
        for (ScheduleEntity schedule : schedules) {
            // Get all students in the class
            List<UserEntity> students = userRepository.findByClassesId(schedule.getClasses().getId());
            
            for (UserEntity student : students) {
                AttendanceScoreDto score = calculateSingleStudentScore(student.getId(), schedule.getId(), null, null);
                allScores.add(score);
            }
        }
        
        // Paginate results manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allScores.size());
        
        return new PageImpl<>(
            allScores.subList(start, end),
            pageable,
            allScores.size()
        );
    }
    
    private AttendanceScoreDto calculateSingleStudentScore(Long studentId, Long scheduleId, 
                                                          LocalDateTime startDate, LocalDateTime endDate) {
        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));
                
        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found with id: " + scheduleId));
        
        // Count total finalized attendance sessions
        long totalSessions;
        if (startDate != null && endDate != null) {
            totalSessions = sessionRepository.countByScheduleIdAndSessionDateBetween(scheduleId, startDate, endDate);
        } else {
            totalSessions = sessionRepository.countByScheduleId(scheduleId);
        }
        
        if (totalSessions == 0) {
            // No sessions yet
            return AttendanceScoreDto.builder()
                    .studentId(studentId)
                    .studentName(student.getName())
                    .studentCode(student.getCode())
                    .scheduleId(scheduleId)
                    .courseName(schedule.getCourse().getName())
                    .className(schedule.getClasses().getCode())
                    .totalSessions(0L)
                    .attendedSessions(0L)
                    .attendancePercentage(100.0) // Perfect attendance (no sessions to attend)
                    .build();
        }
        
        // Count sessions where student was present or late
        Specification<AttendanceSessionEntity> sessionSpec = Specification.where(null);
        
        if (startDate != null && endDate != null) {
            sessionSpec = sessionSpec.and((root, query, cb) -> 
                cb.between(root.get("sessionDate"), startDate, endDate));
        }
        
        sessionSpec = sessionSpec.and((root, query, cb) -> 
            cb.equal(root.get("schedule").get("id"), scheduleId))
            .and((root, query, cb) -> 
                cb.equal(root.get("isFinal"), true));
        
        List<AttendanceSessionEntity> sessions = sessionRepository.findAll(sessionSpec);
        
        long attendedSessions = 0;
        for (AttendanceSessionEntity session : sessions) {
            boolean attended = attendanceRepository.findAll(
                Specification.where(AttendanceSpecification.hasStudentId(studentId))
                    .and(AttendanceSpecification.hasSessionId(session.getId()))
                    .and((root, query, cb) -> {
                        return cb.or(
                            cb.equal(root.get("status"), AttendanceStatus.PRESENT),
                            cb.equal(root.get("status"), AttendanceStatus.LATE)
                        );
                    })
            ).size() > 0;
            
            if (attended) {
                attendedSessions++;
            }
        }
        
        double percentage = (double) attendedSessions / totalSessions * 100;
        
        return AttendanceScoreDto.builder()
                .studentId(studentId)
                .studentName(student.getName())
                .studentCode(student.getCode())
                .scheduleId(scheduleId)
                .courseName(schedule.getCourse().getName())
                .className(schedule.getClasses().getCode())
                .totalSessions(totalSessions)
                .attendedSessions(attendedSessions)
                .attendancePercentage(percentage)
                .build();
    }
    
    private List<AttendanceScoreDto> calculateClassScores(Long classId, Long scheduleId, 
                                                         LocalDateTime startDate, LocalDateTime endDate) {
        // Get all students in the class
        List<UserEntity> students = userRepository.findByClassesId(classId);
        
        return students.stream()
                .map(student -> calculateSingleStudentScore(student.getId(), scheduleId, startDate, endDate))
                .collect(Collectors.toList());
    }
}