package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceScoreDto;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import com.menghor.ksit.feature.attendance.repository.AttendanceRepository;
import com.menghor.ksit.feature.attendance.repository.AttendanceSessionRepository;
import com.menghor.ksit.feature.attendance.service.AttendanceScoreService;
import com.menghor.ksit.feature.attendance.specification.AttendanceSpecification;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.master.repository.SemesterRepository;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceScoreServiceImpl implements AttendanceScoreService {
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final SemesterRepository semesterRepository;


    @Override
    public List<AttendanceScoreDto> calculateForClass(Long classId, Long scheduleId) {
        return calculateClassScores(classId, scheduleId);
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
                    .studentName(student.getEnglishFirstName() + " " + student.getEnglishLastName())
                    .studentCode(student.getIdentifyNumber())
                    .scheduleId(scheduleId)
                    .courseName(schedule.getCourse().getNameEn())
                    .className(schedule.getClasses().getCode())
                    .totalSessions(0L)
                    .attendedSessions(0L)
                    .attendancePercentage(100.0) // Perfect attendance (no sessions to attend)
                    .build();
        }

        // Count sessions where student was present
        Specification<AttendanceEntity> attendanceSpec = Specification.where(AttendanceSpecification.hasStudentId(studentId))
                .and((root, query, cb) -> cb.equal(root.get("attendanceSession").get("schedule").get("id"), scheduleId))
                .and(AttendanceSpecification.hasStatus(AttendanceStatus.PRESENT));

        if (startDate != null && endDate != null) {
            attendanceSpec = attendanceSpec.and((root, query, cb) ->
                    cb.between(root.get("attendanceSession").get("sessionDate"), startDate, endDate));
        }

        long attendedSessions = attendanceRepository.count(attendanceSpec);
        double percentage = (double) attendedSessions / totalSessions * 100;

        return AttendanceScoreDto.builder()
                .studentId(studentId)
                .studentName(student.getEnglishFirstName() + " " + student.getEnglishLastName())
                .studentCode(student.getIdentifyNumber())
                .scheduleId(scheduleId)
                .courseName(schedule.getCourse().getNameEn())
                .className(schedule.getClasses().getCode())
                .totalSessions(totalSessions)
                .attendedSessions(attendedSessions)
                .attendancePercentage(percentage)
                .build();
    }

    private List<AttendanceScoreDto> calculateClassScores(Long classId, Long scheduleId) {
        // Get all students in the class
        List<UserEntity> allUsers = userRepository.findAll();
        List<UserEntity> students = allUsers.stream()
                .filter(u -> u.getClasses() != null && u.getClasses().getId().equals(classId))
                .toList();

        return students.stream()
                .map(student -> calculateSingleStudentScore(student.getId(), scheduleId, null, null))
                .collect(Collectors.toList());
    }
}