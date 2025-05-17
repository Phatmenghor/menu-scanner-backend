package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.attendance.dto.request.BatchUpdateScoresRequestDto;
import com.menghor.ksit.feature.attendance.dto.request.CalculateAttendanceScoresRequestDto;
import com.menghor.ksit.feature.attendance.dto.request.ScoreSessionRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.ScoreSessionUpdateDto;
import com.menghor.ksit.feature.attendance.mapper.ScoreSessionMapper;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.attendance.repository.ScoreSessionRepository;
import com.menghor.ksit.feature.attendance.repository.StudentScoreRepository;
import com.menghor.ksit.feature.attendance.service.ScoreSessionService;
import com.menghor.ksit.feature.attendance.service.StudentScoreService;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
import com.menghor.ksit.utils.database.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreSessionServiceImpl implements ScoreSessionService {

    private final ScoreSessionRepository scoreSessionRepository;
    private final StudentScoreRepository studentScoreRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ScoreSessionMapper scoreSessionMapper;
    private final StudentScoreService studentScoreService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public ScoreSessionResponseDto initializeScoreSession(ScoreSessionRequestDto requestDto) {
        log.info("Initializing score session for schedule ID: {}", requestDto.getScheduleId());
        
        // Check if a score session already exists for this schedule
        Optional<ScoreSessionEntity> existingSession = scoreSessionRepository.findByScheduleId(requestDto.getScheduleId());
        
        if (existingSession.isPresent()) {
            log.info("Found existing score session with ID: {}", existingSession.get().getId());
            
            // Check if there are new students in the class that don't have scores yet
            ScoreSessionEntity session = existingSession.get();
            ScheduleEntity schedule = session.getSchedule();
            List<UserEntity> studentsInClass = userRepository.findByClassesId(schedule.getClasses().getId());
            
            // For each student, ensure they have a score record
            for (UserEntity student : studentsInClass) {
                if (!studentScoreRepository.existsByScoreSessionIdAndStudentId(session.getId(), student.getId())) {
                    log.info("Creating score record for newly added student: {} in session: {}", 
                            student.getId(), session.getId());
                    
                    // Create new student score record
                    StudentScoreEntity studentScore = new StudentScoreEntity();
                    studentScore.setScoreSession(session);
                    studentScore.setStudent(student);
                    
                    // Initialize with default values
                    studentScore.setAttendanceScore(0.0);
                    studentScore.setAssignmentScore(0.0);
                    studentScore.setMidtermScore(0.0);
                    studentScore.setFinalScore(0.0);
                    
                    studentScoreRepository.save(studentScore);
                }
            }
            
            // Refresh from database to get the updated scores
            session = scoreSessionRepository.findById(session.getId()).get();
            
            return scoreSessionMapper.toDto(session);
        }
        
        // Create new score session
        ScheduleEntity schedule = scheduleRepository.findById(requestDto.getScheduleId())
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + requestDto.getScheduleId()));
        
        // Get current user as teacher
        UserEntity currentUser = securityUtils.getCurrentUser();
        
        // Create score session
        ScoreSessionEntity scoreSession = new ScoreSessionEntity();
        scoreSession.setSchedule(schedule);
        scoreSession.setTeacher(currentUser);
        scoreSession.setStatus(SubmissionStatus.DRAFT);
        
        // Save session to get ID
        ScoreSessionEntity savedSession = scoreSessionRepository.save(scoreSession);
        
        // Get all students in the class
        List<UserEntity> students = userRepository.findByClassesId(schedule.getClasses().getId());
        
        // Create score records for each student
        List<StudentScoreEntity> studentScores = new ArrayList<>();
        for (UserEntity student : students) {
            StudentScoreEntity studentScore = new StudentScoreEntity();
            studentScore.setScoreSession(savedSession);
            studentScore.setStudent(student);
            
            // Initially set null or 0 for scores
            studentScore.setAttendanceScore(0.0);
            studentScore.setAssignmentScore(0.0);
            studentScore.setMidtermScore(0.0);
            studentScore.setFinalScore(0.0);
            
            studentScores.add(studentScore);
        }
        
        // Save all student scores
        List<StudentScoreEntity> savedStudentScores = studentScoreRepository.saveAll(studentScores);
        savedSession.setStudentScores(savedStudentScores);
        
        log.info("Score session initialized successfully with ID: {}", savedSession.getId());
        return scoreSessionMapper.toDto(savedSession);
    }

    @Override
    public ScoreSessionResponseDto getScoreSessionById(Long id) {
        log.info("Getting score session with ID: {}", id);
        
        ScoreSessionEntity scoreSession = scoreSessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Score session not found with ID: " + id));
        
        return scoreSessionMapper.toDto(scoreSession);
    }

    @Override
    public ScoreSessionResponseDto getScoreSessionByScheduleId(Long scheduleId) {
        log.info("Getting score session for schedule ID: {}", scheduleId);
        
        ScoreSessionEntity scoreSession = scoreSessionRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new NotFoundException("No score session found for schedule with ID: " + scheduleId));
        
        return scoreSessionMapper.toDto(scoreSession);
    }

    @Override
    @Transactional
    public ScoreSessionResponseDto batchUpdateStudentScores(BatchUpdateScoresRequestDto requestDto) {
        log.info("Batch updating student scores for session ID: {}", requestDto.getScoreSessionId());
        
        // Verify score session exists
        ScoreSessionEntity scoreSession = scoreSessionRepository.findById(requestDto.getScoreSessionId())
                .orElseThrow(() -> new NotFoundException("Score session not found with ID: " + requestDto.getScoreSessionId()));
        
        // Verify session is in DRAFT status
        if (scoreSession.getStatus() != SubmissionStatus.DRAFT && scoreSession.getStatus() != SubmissionStatus.REJECTED) {
            throw new BadRequestException("Cannot update scores. Session is in " + scoreSession.getStatus() + " status");
        }
        
        // Update each student score
        for (var scoreUpdateDto : requestDto.getStudentScores()) {
            studentScoreService.updateStudentScore(scoreUpdateDto);
        }
        
        // Refresh session from database
        scoreSession = scoreSessionRepository.findById(requestDto.getScoreSessionId()).get();
        
        log.info("Successfully updated scores for {} students", requestDto.getStudentScores().size());
        return scoreSessionMapper.toDto(scoreSession);
    }

    @Override
    @Transactional
    public ScoreSessionResponseDto updateScoreSession(ScoreSessionUpdateDto updateDto) {
        log.info("Updating score session with ID: {}", updateDto.getId());
        
        ScoreSessionEntity scoreSession = scoreSessionRepository.findById(updateDto.getId())
                .orElseThrow(() -> new NotFoundException("Score session not found with ID: " + updateDto.getId()));
        
        // Update fields if provided
        if (updateDto.getStatus() != null) {
            scoreSession.setStatus(updateDto.getStatus());
        }
        
        if (updateDto.getTeacherComments() != null) {
            scoreSession.setTeacherComments(updateDto.getTeacherComments());
        }
        
        if (updateDto.getStaffComments() != null) {
            scoreSession.setStaffComments(updateDto.getStaffComments());
        }
        
        ScoreSessionEntity updatedSession = scoreSessionRepository.save(scoreSession);
        log.info("Score session updated successfully");
        
        return scoreSessionMapper.toDto(updatedSession);
    }

    @Override
    @Transactional
    public ScoreSessionResponseDto submitForReview(Long scoreSessionId, String comments) {
        log.info("Submitting score session with ID: {} for review", scoreSessionId);
        
        ScoreSessionEntity scoreSession = scoreSessionRepository.findById(scoreSessionId)
                .orElseThrow(() -> new NotFoundException("Score session not found with ID: " + scoreSessionId));
        
        // Verify session is in DRAFT or REJECTED status
        if (scoreSession.getStatus() != SubmissionStatus.DRAFT && scoreSession.getStatus() != SubmissionStatus.REJECTED) {
            throw new BadRequestException("Cannot submit session. Session is in " + scoreSession.getStatus() + " status");
        }
        
        // Update status and set submission date
        scoreSession.setStatus(SubmissionStatus.SUBMITTED);
        scoreSession.setSubmissionDate(LocalDateTime.now());
        
        // Update teacher comments if provided
        if (comments != null && !comments.trim().isEmpty()) {
            scoreSession.setTeacherComments(comments);
        }
        
        ScoreSessionEntity updatedSession = scoreSessionRepository.save(scoreSession);
        log.info("Score session submitted for review successfully");
        
        return scoreSessionMapper.toDto(updatedSession);
    }

    @Override
    @Transactional
    public ScoreSessionResponseDto reviewScoreSession(Long scoreSessionId, String statusStr, String comments) {
        log.info("Reviewing score session with ID: {}, status: {}", scoreSessionId, statusStr);
        
        ScoreSessionEntity scoreSession = scoreSessionRepository.findById(scoreSessionId)
                .orElseThrow(() -> new NotFoundException("Score session not found with ID: " + scoreSessionId));
        
        // Verify session is in SUBMITTED or PENDING status
        if (scoreSession.getStatus() != SubmissionStatus.SUBMITTED && scoreSession.getStatus() != SubmissionStatus.PENDING) {
            throw new BadRequestException("Cannot review session. Session is in " + scoreSession.getStatus() + " status");
        }
        
        // Parse status
        SubmissionStatus status;
        try {
            status = SubmissionStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + statusStr);
        }
        
        // Only allow valid status transitions
        if (status != SubmissionStatus.APPROVED && status != SubmissionStatus.REJECTED && status != SubmissionStatus.PENDING) {
            throw new BadRequestException("Invalid status for review: " + status);
        }
        
        // Update status, set reviewer and review date
        scoreSession.setStatus(status);
        scoreSession.setReviewer(securityUtils.getCurrentUser());
        scoreSession.setReviewDate(LocalDateTime.now());
        
        // Update staff comments if provided
        if (comments != null && !comments.trim().isEmpty()) {
            scoreSession.setStaffComments(comments);
        }
        
        ScoreSessionEntity updatedSession = scoreSessionRepository.save(scoreSession);
        log.info("Score session reviewed successfully, new status: {}", status);
        
        return scoreSessionMapper.toDto(updatedSession);
    }

    @Override
    @Transactional
    public void calculateAttendanceScores(CalculateAttendanceScoresRequestDto requestDto) {
        log.info("Calculating attendance scores for session ID: {}", requestDto.getScoreSessionId());
        
        ScoreSessionEntity scoreSession = scoreSessionRepository.findById(requestDto.getScoreSessionId())
                .orElseThrow(() -> new NotFoundException("Score session not found with ID: " + requestDto.getScoreSessionId()));
        
        // Calculate attendance score for each student
        for (StudentScoreEntity studentScore : scoreSession.getStudentScores()) {
            try {
                studentScoreService.calculateAttendanceScore(studentScore.getStudent().getId(), requestDto.getScoreSessionId());
            } catch (Exception e) {
                log.error("Error calculating attendance score for student ID: {}", studentScore.getStudent().getId(), e);
                // Continue with next student if one fails
            }
        }
        
        log.info("Attendance scores calculated successfully for {} students", scoreSession.getStudentScores().size());
    }

    @Override
    public List<ScoreSessionResponseDto> getScoreSessionsByTeacherId(Long teacherId) {
        log.info("Getting score sessions for teacher ID: {}", teacherId);
        
        List<ScoreSessionEntity> sessions = scoreSessionRepository.findByTeacherId(teacherId);
        
        return sessions.stream()
                .map(scoreSessionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScoreSessionResponseDto> getScoreSessionsForReview() {
        log.info("Getting score sessions for review");
        
        List<ScoreSessionEntity> sessions = scoreSessionRepository.findByStatus(SubmissionStatus.SUBMITTED);
        
        return sessions.stream()
                .map(scoreSessionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void processNewlyAddedStudent(Long studentId, Long classId) {
        log.info("Processing newly added student ID: {} for class ID: {}", studentId, classId);
        
        // Find all score sessions for this class
        List<ScoreSessionEntity> sessions = scoreSessionRepository.findByClassId(classId);
        
        if (sessions.isEmpty()) {
            log.info("No score sessions found for class ID: {}", classId);
            return;
        }
        
        // Get the student
        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found with ID: " + studentId));
        
        // For each session, ensure the student has a score record
        for (ScoreSessionEntity session : sessions) {
            if (!studentScoreRepository.existsByScoreSessionIdAndStudentId(session.getId(), studentId)) {
                log.info("Creating score record for student ID: {} in session ID: {}", studentId, session.getId());
                
                // Create score record
                StudentScoreEntity studentScore = new StudentScoreEntity();
                studentScore.setScoreSession(session);
                studentScore.setStudent(student);
                
                // Initialize with default values
                studentScore.setAttendanceScore(0.0);
                studentScore.setAssignmentScore(0.0);
                studentScore.setMidtermScore(0.0);
                studentScore.setFinalScore(0.0);
                
                studentScoreRepository.save(studentScore);
                
                // Try to calculate attendance score if possible
                try {
                    studentScoreService.calculateAttendanceScore(studentId, session.getId());
                } catch (Exception e) {
                    log.error("Error calculating attendance score for new student", e);
                    // Don't fail the process if attendance calculation fails
                }
            }
        }
        
        log.info("Successfully processed newly added student for {} score sessions", sessions.size());
    }
}