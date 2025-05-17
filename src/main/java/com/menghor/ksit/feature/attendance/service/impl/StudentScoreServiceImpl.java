package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.AttendanceFinalizationStatus;
import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.StudentScoreUpdateDto;
import com.menghor.ksit.feature.attendance.mapper.StudentScoreMapper;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.attendance.repository.AttendanceRepository;
import com.menghor.ksit.feature.attendance.repository.AttendanceSessionRepository;
import com.menghor.ksit.feature.attendance.repository.ScoreSessionRepository;
import com.menghor.ksit.feature.attendance.repository.StudentScoreRepository;
import com.menghor.ksit.feature.attendance.service.StudentScoreService;
import com.menghor.ksit.feature.attendance.specification.AttendanceSpecification;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentScoreServiceImpl implements StudentScoreService {

    private final StudentScoreRepository studentScoreRepository;
    private final ScoreSessionRepository scoreSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final UserRepository userRepository;
    private final StudentScoreMapper studentScoreMapper;

    @Override
    public StudentScoreResponseDto getStudentScoreById(Long id) {
        log.info("Getting student score with ID: {}", id);
        
        StudentScoreEntity studentScore = studentScoreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student score not found with ID: " + id));
        
        return studentScoreMapper.toDto(studentScore);
    }

    @Override
    @Transactional
    public StudentScoreResponseDto updateStudentScore(StudentScoreUpdateDto updateDto) {
        log.info("Updating student score with ID: {}", updateDto.getId());
        
        StudentScoreEntity studentScore = studentScoreRepository.findById(updateDto.getId())
                .orElseThrow(() -> new NotFoundException("Student score not found with ID: " + updateDto.getId()));
        
        // Update fields if provided
        if (updateDto.getAssignmentScore() != null) {
            studentScore.setAssignmentScore(updateDto.getAssignmentScore());
        }
        
        if (updateDto.getMidtermScore() != null) {
            studentScore.setMidtermScore(updateDto.getMidtermScore());
        }
        
        if (updateDto.getFinalScore() != null) {
            studentScore.setFinalScore(updateDto.getFinalScore());
        }
        
        if (updateDto.getComments() != null) {
            studentScore.setComments(updateDto.getComments());
        }
        
        // Calculate total score and grade
        studentScore.calculateTotalScore();
        
        StudentScoreEntity updatedScore = studentScoreRepository.save(studentScore);
        log.info("Student score updated successfully");
        
        return studentScoreMapper.toDto(updatedScore);
    }

    @Override
    public List<StudentScoreResponseDto> getScoresByStudentId(Long studentId) {
        log.info("Getting scores for student ID: {}", studentId);
        
        List<StudentScoreEntity> scores = studentScoreRepository.findByStudentId(studentId);
        
        return scores.stream()
                .map(studentScoreMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void calculateAttendanceScore(Long studentId, Long scoreSessionId) {
        log.info("Calculating attendance score for student ID: {} in score session ID: {}", studentId, scoreSessionId);
        
        // Find the score session
        ScoreSessionEntity scoreSession = scoreSessionRepository.findById(scoreSessionId)
                .orElseThrow(() -> new NotFoundException("Score session not found with ID: " + scoreSessionId));
        
        // Find the student score record
        StudentScoreEntity studentScore = studentScoreRepository.findByScoreSessionIdAndStudentId(
                scoreSessionId, studentId)
                .orElseThrow(() -> new NotFoundException("No score record found for student ID: " + 
                        studentId + " in score session ID: " + scoreSessionId));
        
        // Get the schedule ID from the score session
        Long scheduleId = scoreSession.getSchedule().getId();
        
        // Count total sessions for this schedule
        long totalSessions = attendanceSessionRepository.countByScheduleId(scheduleId);
        
        if (totalSessions == 0) {
            // No sessions yet, set perfect attendance
            studentScore.setAttendanceScore(100.0);
            studentScoreRepository.save(studentScore);
            log.info("No attendance sessions found, setting perfect attendance score");
            return;
        }
        
        // Create specification to find all finalized attendance records for this student in this schedule
        Specification<AttendanceEntity> spec = Specification
                .where(AttendanceSpecification.hasStudentId(studentId))
                .and((root, query, cb) -> cb.equal(root.get("attendanceSession").get("schedule").get("id"), scheduleId))
                .and(AttendanceSpecification.hasFinalizationStatus(AttendanceFinalizationStatus.FINAL));
        
        // Count total finalized sessions
        long totalFinalizedSessions = attendanceRepository.count(spec);
        
        // Count present sessions
        Specification<AttendanceEntity> presentSpec = spec.and(AttendanceSpecification.hasStatus(AttendanceStatus.PRESENT));
        long presentSessions = attendanceRepository.count(presentSpec);
        
        // Calculate attendance percentage
        double attendancePercentage;
        if (totalFinalizedSessions > 0) {
            attendancePercentage = (double) presentSessions / totalFinalizedSessions * 100;
        } else {
            // No finalized sessions yet
            attendancePercentage = 100.0;
        }
        
        // Update student score
        studentScore.setAttendanceScore(attendancePercentage);
        
        // Recalculate total score
        studentScore.calculateTotalScore();
        
        studentScoreRepository.save(studentScore);
        log.info("Attendance score calculated successfully: {}%", attendancePercentage);
    }

    @Override
    @Transactional
    public void calculateTotalScore(Long studentScoreId) {
        log.info("Calculating total score for student score ID: {}", studentScoreId);
        
        StudentScoreEntity studentScore = studentScoreRepository.findById(studentScoreId)
                .orElseThrow(() -> new NotFoundException("Student score not found with ID: " + studentScoreId));
        
        studentScore.calculateTotalScore();
        
        studentScoreRepository.save(studentScore);
        log.info("Total score calculated successfully");
    }
    
    @Override
    @Transactional
    public StudentScoreResponseDto createStudentScore(Long studentId, Long scoreSessionId) {
        log.info("Creating score record for student ID: {} in session ID: {}", studentId, scoreSessionId);
        
        // Verify student and score session exist
        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found with ID: " + studentId));
        
        ScoreSessionEntity scoreSession = scoreSessionRepository.findById(scoreSessionId)
                .orElseThrow(() -> new NotFoundException("Score session not found with ID: " + scoreSessionId));
        
        // Check if record already exists
        if (studentScoreRepository.existsByScoreSessionIdAndStudentId(scoreSessionId, studentId)) {
            return studentScoreMapper.toDto(
                    studentScoreRepository.findByScoreSessionIdAndStudentId(scoreSessionId, studentId).get());
        }
        
        // Create new student score record
        StudentScoreEntity studentScore = new StudentScoreEntity();
        studentScore.setScoreSession(scoreSession);
        studentScore.setStudent(student);
        
        // Initialize with default values
        studentScore.setAttendanceScore(0.0);
        studentScore.setAssignmentScore(0.0);
        studentScore.setMidtermScore(0.0);
        studentScore.setFinalScore(0.0);
        
        StudentScoreEntity savedScore = studentScoreRepository.save(studentScore);
        
        // Try to calculate attendance score
        try {
            calculateAttendanceScore(studentId, scoreSessionId);
        } catch (Exception e) {
            log.error("Error calculating attendance score for new student score", e);
            // Don't fail the creation if attendance calculation fails
        }
        
        // Refresh from database
        savedScore = studentScoreRepository.findById(savedScore.getId()).get();
        
        log.info("Student score created successfully with ID: {}", savedScore.getId());
        return studentScoreMapper.toDto(savedScore);
    }
}