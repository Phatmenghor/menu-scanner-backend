package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
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
import java.util.Map;
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

            // IMPORTANT: For existing sessions, we preserve ALL previous data
            ScoreSessionEntity session = existingSession.get();
            ScheduleEntity schedule = session.getSchedule();

            // Get current class roster
            List<UserEntity> studentsInClass = userRepository.findByClassesId(schedule.getClasses().getId());

            // Get existing student score records
            List<StudentScoreEntity> existingScores = studentScoreRepository.findByScoreSessionId(session.getId());

            // Create a map of student IDs to existing scores for quick lookup
            Map<Long, StudentScoreEntity> existingScoresMap = existingScores.stream()
                    .collect(Collectors.toMap(score -> score.getStudent().getId(), score -> score));

            // ONLY create new records for students who don't already have one
            List<StudentScoreEntity> newScores = new ArrayList<>();
            for (UserEntity student : studentsInClass) {
                if (!existingScoresMap.containsKey(student.getId())) {
                    log.info("Creating score record for newly added student: {} in session: {}",
                            student.getId(), session.getId());

                    // Create new student score record with default values
                    StudentScoreEntity studentScore = new StudentScoreEntity();
                    studentScore.setScoreSession(session);
                    studentScore.setStudent(student);

                    // Initialize with default values - CONSISTENT POINT-BASED APPROACH
                    studentScore.setAttendanceScore(10.0);  // Full attendance score (10 points)
                    studentScore.setAssignmentScore(0.0);   // No points for assignment yet (out of 20)
                    studentScore.setMidtermScore(0.0);      // No points for midterm yet (out of 30)
                    studentScore.setFinalScore(0.0);        // No points for final yet (out of 40)

                    newScores.add(studentScore);
                }
            }

            // Only save new scores if there are any
            if (!newScores.isEmpty()) {
                studentScoreRepository.saveAll(newScores);
                log.info("Added {} new student records to existing session", newScores.size());
            } else {
                log.info("No new students to add to existing session");
            }

            // Refresh session from database to include any new scores
            session = scoreSessionRepository.findById(session.getId()).get();

            return scoreSessionMapper.toDto(session);
        }

        // Create new score session if none exists
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

            // Initialize with default values - CONSISTENT POINT-BASED APPROACH
            studentScore.setAttendanceScore(10.0);  // Full attendance (10 points)
            studentScore.setAssignmentScore(0.0);   // No assignment points yet (out of 20)
            studentScore.setMidtermScore(0.0);      // No midterm points yet (out of 30)
            studentScore.setFinalScore(0.0);        // No final points yet (out of 40)

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
    public ScoreSessionResponseDto reviewScoreSession(Long scoreSessionId, SubmissionStatus statusStr, String comments) {
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
            status = statusStr;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + statusStr);
        }

        // Only allow valid status transitions
        if (status != SubmissionStatus.APPROVED && status != SubmissionStatus.REJECTED && status != SubmissionStatus.PENDING) {
            throw new BadRequestException("Invalid status for review: " + status);
        }

        // Update status, set reviewer and review date
        scoreSession.setStatus(status);

        // Update staff comments if provided
        if (comments != null && !comments.trim().isEmpty()) {
            scoreSession.setStaffComments(comments);
        }

        ScoreSessionEntity updatedSession = scoreSessionRepository.save(scoreSession);
        log.info("Score session reviewed successfully, new status: {}", status);

        return scoreSessionMapper.toDto(updatedSession);
    }
}