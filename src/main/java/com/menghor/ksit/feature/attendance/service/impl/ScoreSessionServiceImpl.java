package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.attendance.dto.filter.ScoreSessionFilterDto;
import com.menghor.ksit.feature.attendance.dto.request.ScoreSessionRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.ScoreSessionUpdateDto;
import com.menghor.ksit.feature.attendance.mapper.ScoreSessionMapper;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.attendance.repository.ScoreSessionRepository;
import com.menghor.ksit.feature.attendance.repository.StudentScoreRepository;
import com.menghor.ksit.feature.attendance.service.ScoreSessionService;
import com.menghor.ksit.feature.attendance.specification.ScoreSessionSpecification;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.database.SecurityUtils;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public ScoreSessionResponseDto initializeScoreSession(ScoreSessionRequestDto requestDto) {
        log.info("Starting score session initialization for scheduleId={}", requestDto.getScheduleId());

        // Check if a score session already exists for this schedule
        Optional<ScoreSessionEntity> existingSession = scoreSessionRepository.findByScheduleId(requestDto.getScheduleId());

        if (existingSession.isPresent()) {
            return handleExistingSession(existingSession.get());
        } else {
            return createNewSession(requestDto);
        }
    }

    private ScoreSessionResponseDto handleExistingSession(ScoreSessionEntity session) {
        log.info("Found existing score session sessionId={} for scheduleId={}",
                session.getId(), session.getSchedule().getId());

        ScheduleEntity schedule = session.getSchedule();
        Long classId = schedule.getClasses().getId();

        // Get current class roster
        List<UserEntity> studentsInClass = userRepository.findByClassesId(classId);
        log.info("Retrieved {} students from classId={}", studentsInClass.size(), classId);

        // Get existing student score records
        List<StudentScoreEntity> existingScores = studentScoreRepository.findByScoreSessionId(session.getId());
        log.info("Found {} existing student scores for sessionId={}", existingScores.size(), session.getId());

        // Create a map of student IDs to existing scores for quick lookup
        Map<Long, StudentScoreEntity> existingScoresMap = existingScores.stream()
                .collect(Collectors.toMap(score -> score.getStudent().getId(), score -> score));

        // ONLY create new records for students who don't already have one
        List<StudentScoreEntity> newScores = new ArrayList<>();
        for (UserEntity student : studentsInClass) {
            if (!existingScoresMap.containsKey(student.getId())) {
                log.info("Creating score record for new student studentId={} in sessionId={}",
                        student.getId(), session.getId());

                StudentScoreEntity studentScore = createDefaultStudentScore(student, session);
                newScores.add(studentScore);
            }
        }

        // Only save new scores if there are any
        if (!newScores.isEmpty()) {
            studentScoreRepository.saveAll(newScores);
            log.info("Successfully added {} new student records to existing sessionId={}",
                    newScores.size(), session.getId());
        } else {
            log.info("No new students to add to existing sessionId={}", session.getId());
        }

        // Refresh session from database to include any new scores
        session = scoreSessionRepository.findById(session.getId()).get();
        log.info("Score session handling completed for sessionId={}", session.getId());

        return scoreSessionMapper.toDto(session);
    }

    private ScoreSessionResponseDto createNewSession(ScoreSessionRequestDto requestDto) {
        log.info("Creating new score session for scheduleId={}", requestDto.getScheduleId());

        // Find schedule
        ScheduleEntity schedule = scheduleRepository.findById(requestDto.getScheduleId())
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + requestDto.getScheduleId()));

        log.info("Found schedule scheduleId={} with classCode={}",
                requestDto.getScheduleId(), schedule.getClasses().getCode());

        // Get current user as teacher
        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("Current teacher userId={} will be assigned to session", currentUser.getId());

        // Create score session
        ScoreSessionEntity scoreSession = new ScoreSessionEntity();
        scoreSession.setSchedule(schedule);
        scoreSession.setTeacher(currentUser);
        scoreSession.setStatus(SubmissionStatus.DRAFT);

        // Save session to get ID
        ScoreSessionEntity savedSession = scoreSessionRepository.save(scoreSession);
        log.info("Created new score session sessionId={} with status={}",
                savedSession.getId(), savedSession.getStatus());

        // Get all students in the class
        Long classId = schedule.getClasses().getId();
        List<UserEntity> students = userRepository.findByClassesId(classId);
        log.info("Retrieved {} students from classId={} for new session", students.size(), classId);

        // Create score records for each student
        List<StudentScoreEntity> studentScores = new ArrayList<>();
        for (UserEntity student : students) {
            StudentScoreEntity studentScore = createDefaultStudentScore(student, savedSession);
            studentScores.add(studentScore);
        }

        // Save all student scores
        List<StudentScoreEntity> savedStudentScores = studentScoreRepository.saveAll(studentScores);
        savedSession.setStudentScores(savedStudentScores);

        log.info("Score session initialization completed successfully sessionId={} with {} student records",
                savedSession.getId(), savedStudentScores.size());

        return scoreSessionMapper.toDto(savedSession);
    }

    private StudentScoreEntity createDefaultStudentScore(UserEntity student, ScoreSessionEntity scoreSession) {
        StudentScoreEntity studentScore = new StudentScoreEntity();
        studentScore.setScoreSession(scoreSession);
        studentScore.setStudent(student);

        // Initialize with default values - CONSISTENT POINT-BASED APPROACH
        studentScore.setAttendanceScore(10.0);  // Full attendance (10 points)
        studentScore.setAssignmentScore(0.0);   // No assignment points yet (out of 20)
        studentScore.setMidtermScore(0.0);      // No midterm points yet (out of 30)
        studentScore.setFinalScore(0.0);        // No final points yet (out of 40)

        return studentScore;
    }

    @Override
    public ScoreSessionResponseDto getScoreSessionById(Long id) {
        log.info("Retrieving score session sessionId={}", id);

        ScoreSessionEntity scoreSession = scoreSessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Score session not found with ID: " + id));

        log.info("Successfully retrieved score session sessionId={} with status={} and {} student scores",
                id, scoreSession.getStatus(), scoreSession.getStudentScores().size());

        return scoreSessionMapper.toDto(scoreSession);
    }

    @Override
    @Transactional
    public ScoreSessionResponseDto updateScoreSession(ScoreSessionUpdateDto updateDto) {
        log.info("Starting update for score session sessionId={}", updateDto.getId());

        ScoreSessionEntity scoreSession = scoreSessionRepository.findById(updateDto.getId())
                .orElseThrow(() -> new NotFoundException("Score session not found with ID: " + updateDto.getId()));

        SubmissionStatus oldStatus = scoreSession.getStatus();
        boolean hasChanges = false;

        // Update fields if provided
        if (updateDto.getStatus() != null && !updateDto.getStatus().equals(oldStatus)) {
            scoreSession.setStatus(updateDto.getStatus());
            hasChanges = true;
            log.info("Status updated from {} to {} for sessionId={}",
                    oldStatus, updateDto.getStatus(), updateDto.getId());
        }

        if (updateDto.getTeacherComments() != null) {
            scoreSession.setTeacherComments(updateDto.getTeacherComments());
            hasChanges = true;
            log.info("Teacher comments updated for sessionId={}", updateDto.getId());
        }

        if (updateDto.getStaffComments() != null) {
            scoreSession.setStaffComments(updateDto.getStaffComments());
            hasChanges = true;
            log.info("Staff comments updated for sessionId={}", updateDto.getId());
        }

        scoreSession.setSubmissionDate(LocalDateTime.now());

        if (hasChanges) {
            ScoreSessionEntity updatedSession = scoreSessionRepository.save(scoreSession);
            log.info("Score session update completed successfully for sessionId={}", updateDto.getId());
            return scoreSessionMapper.toDto(updatedSession);
        } else {
            log.info("No changes detected for score session sessionId={}", updateDto.getId());
            return scoreSessionMapper.toDto(scoreSession);
        }
    }

    @Override
    public CustomPaginationResponseDto<ScoreSessionResponseDto> getAllScoreSessions(ScoreSessionFilterDto filterDto) {
        log.info("Retrieving score sessions with filters: pageNo={}, pageSize={}, search='{}', status={}, teacherId={}, scheduleId={}, classId={}, courseId={}, studentId={}",
                filterDto.getPageNo(), filterDto.getPageSize(), filterDto.getSearch(),
                filterDto.getStatus(), filterDto.getTeacherId(), filterDto.getScheduleId(),
                filterDto.getClassId(), filterDto.getCourseId(), filterDto.getStudentId());

        // Create pageable object
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Build specification based on filter criteria
        Specification<ScoreSessionEntity> spec = ScoreSessionSpecification.combine(
                filterDto.getSearch(),
                filterDto.getStatus(),
                filterDto.getTeacherId(),
                filterDto.getScheduleId(),
                filterDto.getClassId(),
                filterDto.getCourseId(),
                filterDto.getStudentId()
        );

        // Execute query with pagination
        Page<ScoreSessionEntity> scoreSessionPage = scoreSessionRepository.findAll(spec, pageable);

        log.info("Retrieved {} score sessions on page {} of {} (total elements: {})",
                scoreSessionPage.getContent().size(),
                scoreSessionPage.getNumber() + 1,
                scoreSessionPage.getTotalPages(),
                scoreSessionPage.getTotalElements());

        // Convert entities to DTOs
        List<ScoreSessionResponseDto> content = scoreSessionPage.getContent().stream()
                .map(scoreSessionMapper::toDto)
                .collect(Collectors.toList());

        // Build and return pagination response
        CustomPaginationResponseDto<ScoreSessionResponseDto> response = new CustomPaginationResponseDto<>(
                content,
                scoreSessionPage.getNumber() + 1, // Convert back to 1-based page number
                scoreSessionPage.getSize(),
                scoreSessionPage.getTotalElements(),
                scoreSessionPage.getTotalPages(),
                scoreSessionPage.isLast()
        );

        log.info("Score sessions retrieval completed successfully");
        return response;
    }
}