package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.attendance.dto.filter.ScoreSessionFilterDto;
import com.menghor.ksit.feature.attendance.dto.request.ScoreSessionRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.ScoreSessionUpdateDto;
import com.menghor.ksit.feature.attendance.mapper.ScoreConfigurationMapper;
import com.menghor.ksit.feature.attendance.mapper.ScoreSessionMapper;
import com.menghor.ksit.feature.attendance.mapper.StudentScoreMapper;
import com.menghor.ksit.feature.attendance.models.ScoreConfigurationEntity;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.attendance.repository.ScoreConfigurationRepository;
import com.menghor.ksit.feature.attendance.repository.ScoreSessionRepository;
import com.menghor.ksit.feature.attendance.repository.StudentScoreRepository;
import com.menghor.ksit.feature.attendance.service.ScoreSessionService;
import com.menghor.ksit.feature.attendance.specification.ScoreSessionSpecification;
import com.menghor.ksit.feature.attendance.specification.StudentScoreSpecification;
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

import java.math.BigDecimal;
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

    // Repositories
    private final ScoreSessionRepository scoreSessionRepository;
    private final StudentScoreRepository studentScoreRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ScoreConfigurationRepository scoreConfigRepository;

    // MapStruct Mappers - Injected as dependencies
    private final ScoreSessionMapper scoreSessionMapper;
    private final StudentScoreMapper studentScoreMapper;
    private final ScoreConfigurationMapper scoreConfigurationMapper;

    // Utils
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public ScoreSessionResponseDto initializeScoreSession(ScoreSessionRequestDto requestDto) {
        log.info("Starting score session initialization for scheduleId={}", requestDto.getScheduleId());

        // Use specification to find existing session
        Specification<ScoreSessionEntity> existingSessionSpec = ScoreSessionSpecification
                .hasScheduleId(requestDto.getScheduleId())
                .and(ScoreSessionSpecification.hasStatus(SubmissionStatus.DRAFT));

        Optional<ScoreSessionEntity> existingSession = scoreSessionRepository.findOne(existingSessionSpec);

        if (existingSession.isPresent()) {
            return handleExistingSession(existingSession.get());
        } else {
            return createNewSession(requestDto);
        }
    }

    private ScoreSessionResponseDto handleExistingSession(ScoreSessionEntity session) {
        log.info("Handling existing score session sessionId={}", session.getId());

        ScheduleEntity schedule = session.getSchedule();
        Long classId = schedule.getClasses().getId();

        // Use specification to get students by class
        List<UserEntity> studentsInClass = findStudentsByClass(classId);
        log.info("Retrieved {} students from classId={}", studentsInClass.size(), classId);

        // Use specification to get existing student scores
        List<StudentScoreEntity> existingScores = findStudentScoresBySession(session.getId());
        log.info("Found {} existing student scores", existingScores.size());

        // Create map for quick lookup
        Map<Long, StudentScoreEntity> existingScoresMap = existingScores.stream()
                .collect(Collectors.toMap(score -> score.getStudent().getId(), score -> score));

        // Create new scores for students who don't have them
        List<StudentScoreEntity> newScores = createMissingStudentScores(studentsInClass, existingScoresMap, session);

        if (!newScores.isEmpty()) {
            studentScoreRepository.saveAll(newScores);
            log.info("Added {} new student scores", newScores.size());
        }

        // Refresh and return using mapper
        ScoreSessionEntity refreshedSession = scoreSessionRepository.findById(session.getId()).orElse(session);
        return scoreSessionMapper.toDto(refreshedSession);
    }

    private ScoreSessionResponseDto createNewSession(ScoreSessionRequestDto requestDto) {
        log.info("Creating new score session for scheduleId={}", requestDto.getScheduleId());

        // Find schedule using specification
        ScheduleEntity schedule = findScheduleById(requestDto.getScheduleId());
        UserEntity currentUser = securityUtils.getCurrentUser();

        // Create new session entity
        ScoreSessionEntity scoreSession = createScoreSessionEntity(schedule, currentUser);
        ScoreSessionEntity savedSession = scoreSessionRepository.save(scoreSession);

        // Get students and create their score records
        List<UserEntity> students = findStudentsByClass(schedule.getClasses().getId());
        List<StudentScoreEntity> studentScores = createStudentScoresForSession(students, savedSession);

        studentScoreRepository.saveAll(studentScores);
        savedSession.setStudentScores(studentScores);

        log.info("Created session sessionId={} with {} students", savedSession.getId(), studentScores.size());

        // Use mapper to convert to DTO
        return scoreSessionMapper.toDto(savedSession);
    }

    @Override
    public ScoreSessionResponseDto getScoreSessionById(Long id) {
        log.info("Retrieving score session sessionId={}", id);

        // Use specification to find session with all related data
        Specification<ScoreSessionEntity> spec = ScoreSessionSpecification
                .hasId(id)
                .and(ScoreSessionSpecification.isNotDeleted());

        ScoreSessionEntity scoreSession = scoreSessionRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Score session not found with ID: " + id));

        log.info("Found score session with {} student scores", scoreSession.getStudentScores().size());

        // Use mapper to convert to DTO
        return scoreSessionMapper.toDto(scoreSession);
    }

    @Override
    @Transactional
    public ScoreSessionResponseDto updateScoreSession(ScoreSessionUpdateDto updateDto) {
        log.info("Updating score session sessionId={}", updateDto.getId());

        ScoreSessionEntity scoreSession = scoreSessionRepository.findById(updateDto.getId())
                .orElseThrow(() -> new NotFoundException("Score session not found with ID: " + updateDto.getId()));

        // Update entity fields
        updateScoreSessionFields(scoreSession, updateDto);

        ScoreSessionEntity updatedSession = scoreSessionRepository.save(scoreSession);
        log.info("Score session updated successfully");

        // Use mapper to convert to DTO
        return scoreSessionMapper.toDto(updatedSession);
    }

    @Override
    public CustomPaginationResponseDto<ScoreSessionResponseDto> getAllScoreSessions(ScoreSessionFilterDto filterDto) {
        log.info("Retrieving score sessions with filters: {}", filterDto);

        // Create pageable
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Build complex specification using multiple criteria
        Specification<ScoreSessionEntity> spec = buildScoreSessionSpecification(filterDto);

        // Execute query
        Page<ScoreSessionEntity> scoreSessionPage = scoreSessionRepository.findAll(spec, pageable);

        // Convert entities to DTOs using mapper
        List<ScoreSessionResponseDto> content = scoreSessionPage.getContent().stream()
                .map(scoreSessionMapper::toDto)
                .collect(Collectors.toList());

        log.info("Retrieved {} score sessions", content.size());

        return new CustomPaginationResponseDto<>(
                content,
                scoreSessionPage.getNumber() + 1,
                scoreSessionPage.getSize(),
                scoreSessionPage.getTotalElements(),
                scoreSessionPage.getTotalPages(),
                scoreSessionPage.isLast()
        );
    }

    // Private helper methods using specifications and mappers

    private List<UserEntity> findStudentsByClass(Long classId) {
        // You could create a UserSpecification for this
        return userRepository.findByClassesId(classId);
    }

    private List<StudentScoreEntity> findStudentScoresBySession(Long sessionId) {
        Specification<StudentScoreEntity> spec = StudentScoreSpecification
                .hasScoreSessionId(sessionId)
                .and(StudentScoreSpecification.isNotDeleted());

        return studentScoreRepository.findAll(spec);
    }

    private ScheduleEntity findScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + scheduleId));
    }

    private ScoreSessionEntity createScoreSessionEntity(ScheduleEntity schedule, UserEntity teacher) {
        ScoreSessionEntity scoreSession = new ScoreSessionEntity();
        scoreSession.setSchedule(schedule);
        scoreSession.setTeacher(teacher);
        scoreSession.setStatus(SubmissionStatus.DRAFT);
        scoreSession.setSubmissionDate(null);
        return scoreSession;
    }

    private List<StudentScoreEntity> createMissingStudentScores(
            List<UserEntity> studentsInClass,
            Map<Long, StudentScoreEntity> existingScoresMap,
            ScoreSessionEntity session) {

        return studentsInClass.stream()
                .filter(student -> !existingScoresMap.containsKey(student.getId()))
                .map(student -> createDefaultStudentScore(student, session))
                .collect(Collectors.toList());
    }

    private List<StudentScoreEntity> createStudentScoresForSession(List<UserEntity> students, ScoreSessionEntity session) {
        return students.stream()
                .map(student -> createDefaultStudentScore(student, session))
                .collect(Collectors.toList());
    }

    private StudentScoreEntity createDefaultStudentScore(UserEntity student, ScoreSessionEntity scoreSession) {
        StudentScoreEntity studentScore = new StudentScoreEntity();
        studentScore.setScoreSession(scoreSession);
        studentScore.setStudent(student);

        // Get active score configuration using specification
        Specification<ScoreConfigurationEntity> configSpec = Specification
                .where((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("status"), Status.ACTIVE));

        Optional<ScoreConfigurationEntity> scoreConfig = scoreConfigRepository.findOne(configSpec);
        scoreConfig.ifPresent(studentScore::setScoreConfiguration);

        // Initialize default scores
        studentScore.setAttendanceRawScore(BigDecimal.valueOf(100.0));
        studentScore.setAssignmentRawScore(BigDecimal.ZERO);
        studentScore.setMidtermRawScore(BigDecimal.ZERO);
        studentScore.setFinalRawScore(BigDecimal.ZERO);

        return studentScore;
    }

    private void updateScoreSessionFields(ScoreSessionEntity scoreSession, ScoreSessionUpdateDto updateDto) {
        if (updateDto.getStatus() != null) {
            scoreSession.setStatus(updateDto.getStatus());
            if (updateDto.getStatus() == SubmissionStatus.SUBMITTED) {
                scoreSession.setSubmissionDate(LocalDateTime.now());
            }
        }

        if (updateDto.getTeacherComments() != null) {
            scoreSession.setTeacherComments(updateDto.getTeacherComments());
        }

        if (updateDto.getStaffComments() != null) {
            scoreSession.setStaffComments(updateDto.getStaffComments());
        }
    }

    private Specification<ScoreSessionEntity> buildScoreSessionSpecification(ScoreSessionFilterDto filterDto) {
        return Specification
                .where(ScoreSessionSpecification.searchByNameOrCode(filterDto.getSearch()))
                .and(ScoreSessionSpecification.hasStatus(filterDto.getStatus()))
                .and(ScoreSessionSpecification.hasTeacherId(filterDto.getTeacherId()))
                .and(ScoreSessionSpecification.hasScheduleId(filterDto.getScheduleId()))
                .and(ScoreSessionSpecification.hasClassId(filterDto.getClassId()))
                .and(ScoreSessionSpecification.hasCourseId(filterDto.getCourseId()))
                .and(ScoreSessionSpecification.hasStudentId(filterDto.getStudentId()));
    }
}