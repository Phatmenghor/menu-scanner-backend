package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceScoreDto;
import com.menghor.ksit.feature.attendance.dto.response.CourseAttendanceDto;
import com.menghor.ksit.feature.attendance.dto.response.StudentAttendanceReportDto;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import com.menghor.ksit.feature.attendance.repository.AttendanceRepository;
import com.menghor.ksit.feature.attendance.repository.AttendanceSessionRepository;
import com.menghor.ksit.feature.attendance.service.AttendanceScoreService;
import com.menghor.ksit.feature.attendance.specification.AttendanceSpecification;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.feature.master.repository.SemesterRepository;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
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
public class AttendanceScoreServiceImpl implements AttendanceScoreService {
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final SemesterRepository semesterRepository;

    @Override
    public AttendanceScoreDto calculateForStudent(Long studentId, Long scheduleId) {
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
        List<ScheduleEntity> allSchedules = scheduleRepository.findAll();
        List<ScheduleEntity> schedules = allSchedules.stream()
                .filter(s -> s.getCourse().getId().equals(courseId) &&
                        s.getSemester().getId().equals(semesterId))
                .toList();

        if (schedules.isEmpty()) {
            return Page.empty(pageable);
        }

        List<AttendanceScoreDto> allScores = new ArrayList<>();

        for (ScheduleEntity schedule : schedules) {
            // Get all students in the class
            List<UserEntity> allUsers = userRepository.findAll();
            List<UserEntity> students = allUsers.stream()
                    .filter(u -> u.getClasses() != null &&
                            u.getClasses().getId().equals(schedule.getClasses().getId()))
                    .toList();

            for (UserEntity student : students) {
                AttendanceScoreDto score = calculateSingleStudentScore(student.getId(), schedule.getId(), null, null);
                allScores.add(score);
            }
        }

        // Paginate results manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allScores.size());

        if (start >= allScores.size()) {
            return Page.empty(pageable);
        }

        return new PageImpl<>(
                allScores.subList(start, end),
                pageable,
                allScores.size()
        );
    }

    @Override
    public StudentAttendanceReportDto getStudentAttendanceReport(Long studentId, Long semesterId) {
        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));

        SemesterEntity semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new EntityNotFoundException("Semester not found with id: " + semesterId));

        // Get all schedules for this student's class in the given semester
        List<ScheduleEntity> allSchedules = scheduleRepository.findAll();
        List<ScheduleEntity> schedules = allSchedules.stream()
                .filter(s -> s.getClasses().getId().equals(student.getClasses().getId()) &&
                        s.getSemester().getId().equals(semesterId))
                .toList();

        List<CourseAttendanceDto> courseAttendances = new ArrayList<>();
        double totalPercentage = 0;

        for (ScheduleEntity schedule : schedules) {
            long totalSessions = sessionRepository.countByScheduleId(schedule.getId());

            if (totalSessions == 0) continue;

            // Use specification to count attended sessions
            Specification<AttendanceEntity> attendanceSpec = Specification.where(AttendanceSpecification.hasStudentId(studentId))
                    .and((root, query, cb) -> cb.equal(root.get("attendanceSession").get("schedule").get("id"), schedule.getId()))
                    .and(AttendanceSpecification.hasStatus(AttendanceStatus.PRESENT));

            long attendedSessions = attendanceRepository.count(attendanceSpec);

            double percentage = (double) attendedSessions / totalSessions * 100;
            totalPercentage += percentage;

            CourseAttendanceDto courseAttendance = CourseAttendanceDto.builder()
                    .courseId(schedule.getCourse().getId())
                    .courseName(schedule.getCourse().getNameEn())
                    .scheduleId(schedule.getId())
                    .totalSessions(totalSessions)
                    .attendedSessions(attendedSessions)
                    .attendancePercentage(percentage)
                    .build();

            courseAttendances.add(courseAttendance);
        }

        double overallPercentage = courseAttendances.isEmpty() ?
                100.0 : totalPercentage / courseAttendances.size();

        return StudentAttendanceReportDto.builder()
                .studentId(studentId)
                .studentName(student.getEnglishFirstName() + " " + student.getEnglishLastName())
                .studentCode(student.getIdentifyNumber())
                .className(student.getClasses().getCode())
                .academicYear(String.valueOf(semester.getAcademyYear()))
                .semester(semester.getSemester().name())
                .courses(courseAttendances)
                .overallAttendancePercentage(overallPercentage)
                .build();
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

    private List<AttendanceScoreDto> calculateClassScores(Long classId, Long scheduleId,
                                                          LocalDateTime startDate, LocalDateTime endDate) {
        // Get all students in the class
        List<UserEntity> allUsers = userRepository.findAll();
        List<UserEntity> students = allUsers.stream()
                .filter(u -> u.getClasses() != null && u.getClasses().getId().equals(classId))
                .toList();

        return students.stream()
                .map(student -> calculateSingleStudentScore(student.getId(), scheduleId, startDate, endDate))
                .collect(Collectors.toList());
    }
}