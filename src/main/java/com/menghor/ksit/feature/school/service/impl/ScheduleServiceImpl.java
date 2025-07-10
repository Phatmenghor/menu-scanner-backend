package com.menghor.ksit.feature.school.service.impl;

import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.SurveyStatus;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.RoomEntity;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.feature.master.repository.ClassRepository;
import com.menghor.ksit.feature.master.repository.RoomRepository;
import com.menghor.ksit.feature.master.repository.SemesterRepository;
import com.menghor.ksit.feature.school.dto.filter.ScheduleFilterDto;
import com.menghor.ksit.feature.school.dto.request.ScheduleBulkDuplicateRequestDto;
import com.menghor.ksit.feature.school.dto.request.ScheduleRequestDto;
import com.menghor.ksit.feature.school.dto.response.ScheduleBulkDuplicateResponseDto;
import com.menghor.ksit.feature.school.dto.response.ScheduleResponseDto;
import com.menghor.ksit.feature.school.dto.update.ScheduleUpdateDto;
import com.menghor.ksit.feature.school.helper.ScheduleFilterHelper;
import com.menghor.ksit.feature.school.mapper.ScheduleMapper;
import com.menghor.ksit.feature.school.model.CourseEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.CourseRepository;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
import com.menghor.ksit.feature.school.service.ScheduleService;
import com.menghor.ksit.feature.school.specification.ScheduleSpecification;
import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import com.menghor.ksit.feature.survey.repository.SurveyResponseRepository;
import com.menghor.ksit.feature.survey.service.SurveyService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.database.SecurityUtils;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final SemesterRepository semesterRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final ScheduleMapper scheduleMapper;
    private final SecurityUtils securityUtils;
    private final ScheduleFilterHelper filterHelper;

    @Override
    @Transactional
    public ScheduleResponseDto createSchedule(ScheduleRequestDto requestDto) {
        log.info("Creating new schedule for class ID: {}, day: {}, time: {}-{}",
                requestDto.getClassId(), requestDto.getDay(), requestDto.getStartTime(), requestDto.getEndTime());

        // Validate all required relationships
        ClassEntity classEntity = findClassById(requestDto.getClassId());
        UserEntity teacher = findUserById(requestDto.getTeacherId());
        CourseEntity course = findCourseById(requestDto.getCourseId());
        RoomEntity room = findRoomById(requestDto.getRoomId());
        SemesterEntity semester = findSemesterById(requestDto.getSemesterId());

        // Create new schedule entity using MapStruct
        ScheduleEntity schedule = scheduleMapper.toEntity(requestDto);

        // Set relationships
        schedule.setClasses(classEntity);
        schedule.setUser(teacher);
        schedule.setCourse(course);
        schedule.setRoom(room);
        schedule.setSemester(semester);

        // Set default status if not provided
        if (schedule.getStatus() == null) {
            schedule.setStatus(Status.ACTIVE);
        }

        ScheduleEntity savedSchedule = scheduleRepository.save(schedule);
        log.info("Schedule created successfully with ID: {}", savedSchedule.getId());

        return scheduleMapper.toResponseDto(savedSchedule);
    }

    @Override
    public ScheduleResponseDto getScheduleById(Long id) {
        log.info("Fetching schedule by ID: {}", id);
        ScheduleEntity schedule = findScheduleById(id);
        ScheduleResponseDto responseDto = scheduleMapper.toResponseDto(schedule);

        // Add survey status for current user
        addSurveyStatusToSchedule(responseDto);

        return responseDto;
    }

    @Override
    @Transactional
    public ScheduleResponseDto updateSchedule(Long id, ScheduleUpdateDto updateDto) {
        log.info("Updating schedule with ID: {}", id);

        ScheduleEntity existingSchedule = findScheduleById(id);

        // Use MapStruct to update fields
        scheduleMapper.updateEntityFromDto(updateDto, existingSchedule);

        // Update relationships if provided
        updateRelationships(existingSchedule, updateDto);

        ScheduleEntity updatedSchedule = scheduleRepository.save(existingSchedule);
        log.info("Schedule updated successfully with ID: {}", id);

        return scheduleMapper.toResponseDto(updatedSchedule);
    }

    @Override
    @Transactional
    public ScheduleResponseDto deleteSchedule(Long id) {
        log.info("Deleting schedule with ID: {}", id);
        ScheduleEntity schedule = findScheduleById(id);
        scheduleRepository.delete(schedule);
        log.info("Schedule deleted successfully with ID: {}", id);
        return scheduleMapper.toResponseDto(schedule);
    }

    @Override
    public CustomPaginationResponseDto<ScheduleResponseDto> getAllSchedules(ScheduleFilterDto filterDto) {
        log.info("Fetching all schedules with filter: {}", filterDto);

        // Create specification using the helper
        Specification<ScheduleEntity> spec = ScheduleSpecification.createSpecification(filterDto, userRepository);

        // Create pageable with custom sorting
        Pageable pageable = createSchedulePageable(filterDto);

        // Execute query
        Page<ScheduleEntity> schedulePage = scheduleRepository.findAll(spec, pageable);

        // Convert to response DTO using MapStruct and add survey status
        CustomPaginationResponseDto<ScheduleResponseDto> response = scheduleMapper.toScheduleAllResponseDto(schedulePage);

        // Apply custom sorting to ensure proper day-time order
        response.setContent(applySortingToSchedules(response.getContent()));

        // Add survey status to each schedule for current user (if user is a student)
        try {
            UserEntity currentUser = securityUtils.getCurrentUser();
            addSurveyStatusToSchedules(response.getContent(), currentUser.getId());
        } catch (Exception e) {
            log.debug("Could not determine current user or add survey status: {}", e.getMessage());
            // Set all schedules to NONE status when no user context
            response.getContent().forEach(schedule -> {
                schedule.setSurveyStatus(SurveyStatus.NONE);
                schedule.setSurveySubmittedAt(null);
                schedule.setSurveyResponseId(null);
            });
        }

        log.info("Retrieved {} schedules (page {}/{})",
                response.getContent().size(), response.getPageNo(), response.getTotalPages());

        return response;
    }

    @Override
    public List<ScheduleResponseDto> getAllSchedulesSimple(ScheduleFilterDto filterDto) {
        log.info("Fetching all schedules without pagination with filter: {}", filterDto);

        // Create specification using the helper
        Specification<ScheduleEntity> spec = ScheduleSpecification.createSpecification(filterDto, userRepository);

        // Execute query without pagination first (we'll sort manually)
        List<ScheduleEntity> schedules = scheduleRepository.findAll(spec);

        // DEBUG: Log what days we have in the data
        Map<DayOfWeek, Long> dayCount = schedules.stream()
                .filter(s -> s.getDay() != null)
                .collect(Collectors.groupingBy(
                        ScheduleEntity::getDay,
                        Collectors.counting()
                ));

        log.info("Schedule distribution by day: {}", dayCount);
        log.info("Total schedules found: {}", schedules.size());

        // Convert to response DTOs
        List<ScheduleResponseDto> responseDtos = scheduleMapper.toResponseDtoList(schedules);

        // Apply custom sorting: Monday to Sunday, then by time
        responseDtos = applySortingToSchedules(responseDtos);

        // DEBUG: Log the actual sorting result
        responseDtos.forEach(schedule ->
                log.debug("Schedule ID: {}, Day: {}, Time: {} - {}",
                        schedule.getId(),
                        schedule.getDay(),
                        schedule.getStartTime(),
                        schedule.getEndTime())
        );

        // Add survey status to each schedule for current user (if user is a student)
        try {
            UserEntity currentUser = securityUtils.getCurrentUser();
            addSurveyStatusToSchedules(responseDtos, currentUser.getId());
        } catch (Exception e) {
            log.debug("Could not determine current user or add survey status: {}", e.getMessage());
            // Set all schedules to NONE status when no user context
            responseDtos.forEach(schedule -> {
                schedule.setSurveyStatus(SurveyStatus.NONE);
                schedule.setSurveySubmittedAt(null);
                schedule.setSurveyResponseId(null);
            });
        }

        log.info("Retrieved {} schedules without pagination", responseDtos.size());
        return responseDtos;
    }

    @Override
    public CustomPaginationResponseDto<ScheduleResponseDto> getMySchedules(ScheduleFilterDto filterDto) {
        log.info("Fetching user-specific schedules with filter: {}", filterDto);

        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("Current user: {} with roles: {}", currentUser.getUsername(),
                currentUser.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()));

        // Determine user access level
        if (hasAdminAccess(currentUser)) {
            log.info("User has admin access, returning all schedules");
            return getAllSchedules(filterDto);
        } else if (isTeacherOrStaff(currentUser)) {
            log.info("User is teacher/staff, filtering by teacher ID: {}", currentUser.getId());
            return getSchedulesForTeacher(currentUser.getId(), filterDto);
        } else if (isStudent(currentUser)) {
            log.info("User is student, filtering by class ID: {}",
                    currentUser.getClasses() != null ? currentUser.getClasses().getId() : "none");
            return getSchedulesForStudent(currentUser, filterDto);
        } else {
            log.warn("User {} has unknown or no roles, returning empty schedules", currentUser.getUsername());
            return filterHelper.createEmptyResponse(filterDto);
        }
    }

    // Add to ScheduleServiceImpl.java implementation
    @Override
    @Transactional
    public ScheduleBulkDuplicateResponseDto bulkDuplicateSchedules(ScheduleBulkDuplicateRequestDto requestDto) {
        log.info("Starting bulk duplication from class: {} semester: {} to class: {} semester: {}",
                requestDto.getSourceClassId(), requestDto.getSourceSemesterId(),
                requestDto.getTargetClassId(), requestDto.getTargetSemesterId());

        // Validate source and target entities
        ClassEntity sourceClass = findClassById(requestDto.getSourceClassId());
        SemesterEntity sourceSemester = findSemesterById(requestDto.getSourceSemesterId());
        ClassEntity targetClass = findClassById(requestDto.getTargetClassId());
        SemesterEntity targetSemester = findSemesterById(requestDto.getTargetSemesterId());

        // Find all schedules for source class and semester
        List<ScheduleEntity> sourceSchedules = scheduleRepository.findAll((root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("classes").get("id"), requestDto.getSourceClassId()),
                        criteriaBuilder.equal(root.get("semester").get("id"), requestDto.getSourceSemesterId()),
                        criteriaBuilder.equal(root.get("status"), Status.ACTIVE)
                ));

        log.info("Found {} schedules to duplicate", sourceSchedules.size());

        if (sourceSchedules.isEmpty()) {
            return createEmptyDuplicateResponse(requestDto, sourceClass, sourceSemester, targetClass, targetSemester);
        }

        List<ScheduleResponseDto> duplicatedSchedules = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (ScheduleEntity sourceSchedule : sourceSchedules) {
            try {
                // Check if similar schedule already exists in target
                boolean exists = checkIfScheduleExists(sourceSchedule, requestDto.getTargetClassId(),
                        requestDto.getTargetSemesterId());

                if (exists) {
                    log.info("Schedule already exists for day: {} time: {}-{}, skipping",
                            sourceSchedule.getDay(), sourceSchedule.getStartTime(), sourceSchedule.getEndTime());
                    skippedCount++;
                    continue;
                }

                // Create new schedule
                ScheduleEntity newSchedule = createDuplicateSchedule(sourceSchedule, targetClass, targetSemester);
                ScheduleEntity savedSchedule = scheduleRepository.save(newSchedule);

                ScheduleResponseDto duplicatedDto = scheduleMapper.toResponseDto(savedSchedule);
                duplicatedSchedules.add(duplicatedDto);
                successCount++;

                log.info("Successfully duplicated schedule ID: {} to new ID: {}",
                        sourceSchedule.getId(), savedSchedule.getId());

            } catch (Exception e) {
                failedCount++;
                String error = String.format("Failed to duplicate schedule ID %d: %s",
                        sourceSchedule.getId(), e.getMessage());
                errors.add(error);
                log.error("Error duplicating schedule ID: {}", sourceSchedule.getId(), e);
            }
        }

        // Create response using MapStruct
        ScheduleBulkDuplicateResponseDto response = scheduleMapper.toBulkDuplicateResponse(
                requestDto.getSourceClassId(), sourceClass, requestDto.getSourceSemesterId(), sourceSemester,
                requestDto.getTargetClassId(), targetClass, requestDto.getTargetSemesterId(), targetSemester
        );

        // Set the count fields
        response.setTotalSourceSchedules(sourceSchedules.size());
        response.setSuccessfullyDuplicated(successCount);
        response.setSkipped(skippedCount);
        response.setFailed(failedCount);
        response.setDuplicatedSchedules(duplicatedSchedules);
        response.setErrors(errors);

        String message = String.format("Bulk duplication completed: %d/%d schedules duplicated from %s %s %d to %s %s %d. %d skipped, %d failed.",
                successCount, sourceSchedules.size(),
                sourceClass.getCode(), sourceSemester.getSemester().name(), sourceSemester.getAcademyYear(),
                targetClass.getCode(), targetSemester.getSemester().name(), targetSemester.getAcademyYear(),
                skippedCount, failedCount);
        response.setMessage(message);

        log.info("Bulk duplication completed: {}", message);
        return response;
    }

    private boolean checkIfScheduleExists(ScheduleEntity sourceSchedule, Long targetClassId, Long targetSemesterId) {
        return scheduleRepository.exists((root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("classes").get("id"), targetClassId),
                        criteriaBuilder.equal(root.get("semester").get("id"), targetSemesterId),
                        criteriaBuilder.equal(root.get("day"), sourceSchedule.getDay()),
                        criteriaBuilder.equal(root.get("startTime"), sourceSchedule.getStartTime()),
                        criteriaBuilder.equal(root.get("endTime"), sourceSchedule.getEndTime()),
                        criteriaBuilder.equal(root.get("room").get("id"), sourceSchedule.getRoom().getId()),
                        criteriaBuilder.equal(root.get("status"), Status.ACTIVE)
                ));
    }

    private ScheduleEntity createDuplicateSchedule(ScheduleEntity sourceSchedule, ClassEntity targetClass,
                                                   SemesterEntity targetSemester) {
        // Use MapStruct to duplicate the schedule
        ScheduleEntity newSchedule = scheduleMapper.duplicateSchedule(sourceSchedule);

        // Set the new class and semester (MapStruct ignores these)
        newSchedule.setClasses(targetClass);
        newSchedule.setSemester(targetSemester);

        return newSchedule;
    }

    private ScheduleBulkDuplicateResponseDto createEmptyDuplicateResponse(ScheduleBulkDuplicateRequestDto requestDto,
                                                                          ClassEntity sourceClass, SemesterEntity sourceSemester,
                                                                          ClassEntity targetClass, SemesterEntity targetSemester) {
        // Use MapStruct to create base response
        ScheduleBulkDuplicateResponseDto response = scheduleMapper.toBulkDuplicateResponse(
                requestDto.getSourceClassId(), sourceClass, requestDto.getSourceSemesterId(), sourceSemester,
                requestDto.getTargetClassId(), targetClass, requestDto.getTargetSemesterId(), targetSemester
        );

        // Set the remaining fields
        response.setTotalSourceSchedules(0);
        response.setSuccessfullyDuplicated(0);
        response.setSkipped(0);
        response.setFailed(0);
        response.setDuplicatedSchedules(new ArrayList<>());
        response.setErrors(new ArrayList<>());
        response.setMessage("No schedules found to duplicate");

        return response;
    }

    @Override
    public List<ScheduleResponseDto> getMySchedulesSimple(ScheduleFilterDto filterDto) {
        log.info("Fetching user-specific schedules without pagination with filter: {}", filterDto);

        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("Current user: {} with roles: {}", currentUser.getUsername(),
                currentUser.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()));

        // Determine user access level
        if (hasAdminAccess(currentUser)) {
            log.info("User has admin access, returning all schedules");
            return getAllSchedulesSimple(filterDto);
        } else if (isTeacherOrStaff(currentUser)) {
            log.info("User is teacher/staff, filtering by teacher ID: {}", currentUser.getId());
            return getSchedulesForTeacherSimple(currentUser.getId(), filterDto);
        } else if (isStudent(currentUser)) {
            log.info("User is student, filtering by class ID: {}",
                    currentUser.getClasses() != null ? currentUser.getClasses().getId() : "none");
            return getSchedulesForStudentSimple(currentUser, filterDto);
        } else {
            log.warn("User {} has unknown or no roles, returning empty schedules", currentUser.getUsername());
            return new ArrayList<>();
        }
    }

    // ===== Private Helper Methods =====

    /**
     * Apply custom sorting: Monday to Sunday, then 12 AM to 12 PM
     * This method ensures proper ordering regardless of database sorting
     */
    private List<ScheduleResponseDto> applySortingToSchedules(List<ScheduleResponseDto> schedules) {
        // Define the day order explicitly
        Map<DayOfWeek, Integer> dayOrder = Map.of(
                DayOfWeek.MONDAY, 1,
                DayOfWeek.TUESDAY, 2,
                DayOfWeek.WEDNESDAY, 3,
                DayOfWeek.THURSDAY, 4,
                DayOfWeek.FRIDAY, 5,
                DayOfWeek.SATURDAY, 6,
                DayOfWeek.SUNDAY, 7
        );

        return schedules.stream()
                .sorted(Comparator
                        .comparing((ScheduleResponseDto s) ->
                                dayOrder.getOrDefault(s.getDay(), 999)) // Monday=1, Tuesday=2, ..., Sunday=7
                        .thenComparing(s ->
                                s.getStartTime() != null ? s.getStartTime() : LocalTime.MAX) // 00:00 to 23:59
                        .thenComparing(s ->
                                s.getEndTime() != null ? s.getEndTime() : LocalTime.MAX) // For same start times
                )
                .collect(Collectors.toList());
    }

    private void updateRelationships(ScheduleEntity schedule, ScheduleUpdateDto updateDto) {
        if (updateDto.getClassId() != null) {
            schedule.setClasses(findClassById(updateDto.getClassId()));
        }
        if (updateDto.getTeacherId() != null) {
            schedule.setUser(findUserById(updateDto.getTeacherId()));
        }
        if (updateDto.getCourseId() != null) {
            schedule.setCourse(findCourseById(updateDto.getCourseId()));
        }
        if (updateDto.getRoomId() != null) {
            schedule.setRoom(findRoomById(updateDto.getRoomId()));
        }
        if (updateDto.getSemesterId() != null) {
            schedule.setSemester(findSemesterById(updateDto.getSemesterId()));
        }
    }

    private CustomPaginationResponseDto<ScheduleResponseDto> getSchedulesForTeacher(Long teacherId, ScheduleFilterDto filterDto) {
        Specification<ScheduleEntity> spec = ScheduleSpecification.createTeacherSpecification(teacherId, filterDto);
        Pageable pageable = createSchedulePageable(filterDto);
        Page<ScheduleEntity> schedulePage = scheduleRepository.findAll(spec, pageable);

        CustomPaginationResponseDto<ScheduleResponseDto> response = scheduleMapper.toScheduleAllResponseDto(schedulePage);

        // Apply custom sorting
        response.setContent(applySortingToSchedules(response.getContent()));

        log.info("Retrieved {} schedules for teacher (page {}/{})",
                response.getContent().size(), response.getPageNo(), response.getTotalPages());

        return response;
    }

    private List<ScheduleResponseDto> getSchedulesForTeacherSimple(Long teacherId, ScheduleFilterDto filterDto) {
        Specification<ScheduleEntity> spec = ScheduleSpecification.createTeacherSpecification(teacherId, filterDto);
        List<ScheduleEntity> schedules = scheduleRepository.findAll(spec);

        List<ScheduleResponseDto> responseDtos = scheduleMapper.toResponseDtoList(schedules);

        // Apply custom sorting
        responseDtos = applySortingToSchedules(responseDtos);

        log.info("Retrieved {} schedules for teacher without pagination", responseDtos.size());

        return responseDtos;
    }

    private CustomPaginationResponseDto<ScheduleResponseDto> getSchedulesForStudent(UserEntity student, ScheduleFilterDto filterDto) {
        if (student.getClasses() == null) {
            log.warn("Student {} has no class assigned", student.getUsername());
            return filterHelper.createEmptyResponse(filterDto);
        }

        Specification<ScheduleEntity> spec = ScheduleSpecification.createStudentSpecification(
                student.getClasses().getId(), filterDto);
        Pageable pageable = createSchedulePageable(filterDto);
        Page<ScheduleEntity> schedulePage = scheduleRepository.findAll(spec, pageable);

        CustomPaginationResponseDto<ScheduleResponseDto> response = scheduleMapper.toScheduleAllResponseDto(schedulePage);

        // Apply custom sorting
        response.setContent(applySortingToSchedules(response.getContent()));

        // ALWAYS add survey status for student - this is critical for frontend alerts
        addSurveyStatusToSchedules(response.getContent(), student.getId());

        log.info("Retrieved {} schedules for student (page {}/{})",
                response.getContent().size(), response.getPageNo(), response.getTotalPages());

        return response;
    }

    private List<ScheduleResponseDto> getSchedulesForStudentSimple(UserEntity student, ScheduleFilterDto filterDto) {
        if (student.getClasses() == null) {
            log.warn("Student {} has no class assigned", student.getUsername());
            return new ArrayList<>();
        }

        Specification<ScheduleEntity> spec = ScheduleSpecification.createStudentSpecification(
                student.getClasses().getId(), filterDto);
        List<ScheduleEntity> schedules = scheduleRepository.findAll(spec);

        List<ScheduleResponseDto> responseDtos = scheduleMapper.toResponseDtoList(schedules);

        // Apply custom sorting
        responseDtos = applySortingToSchedules(responseDtos);

        // ALWAYS add survey status for student - this is critical for frontend alerts
        addSurveyStatusToSchedules(responseDtos, student.getId());

        log.info("Retrieved {} schedules for student without pagination", responseDtos.size());
        return responseDtos;
    }

    private void addSurveyStatusToSchedules(List<ScheduleResponseDto> schedules, Long userId) {
        log.info("Adding survey status for {} schedules for user ID: {}", schedules.size(), userId);

        // Get current user to check roles and enrollment
        UserEntity currentUser = null;
        try {
            currentUser = securityUtils.getCurrentUser();
        } catch (Exception e) {
            log.debug("Could not get current user: {}", e.getMessage());
        }

        for (ScheduleResponseDto schedule : schedules) {
            try {
                // Set survey status based on user role and enrollment
                SurveyStatus status = determineSurveyStatus(currentUser, schedule, userId);
                schedule.setSurveyStatus(status);

                // Set submission details based on status
                if (status == SurveyStatus.COMPLETED) {
                    setSurveyCompletionDetails(schedule, userId);
                } else {
                    // Clear submission details for non-completed surveys
                    schedule.setSurveySubmittedAt(null);
                    schedule.setSurveyResponseId(null);
                }

                log.debug("Schedule ID: {} - Survey Status: {}", schedule.getId(), status);

            } catch (Exception e) {
                log.error("Error checking survey status for schedule {} and user {}: {}",
                        schedule.getId(), userId, e.getMessage());

                // Set default values on error
                schedule.setSurveyStatus(SurveyStatus.NONE);
                schedule.setSurveySubmittedAt(null);
                schedule.setSurveyResponseId(null);
            }
        }

        // Log summary for debugging
        Map<SurveyStatus, Long> statusCount = schedules.stream()
                .collect(Collectors.groupingBy(
                        ScheduleResponseDto::getSurveyStatus,
                        Collectors.counting()
                ));

        log.info("Survey status summary for user {}: {}", userId, statusCount);
    }

    /**
     * Determine survey status based on user role and enrollment
     */
    private SurveyStatus determineSurveyStatus(UserEntity currentUser, ScheduleResponseDto schedule, Long userId) {
        // Case 1: No current user or user is not a student
        if (currentUser == null || !isStudent(currentUser)) {
            log.debug("User {} is not a student, setting survey status to NONE", userId);
            return SurveyStatus.NONE;
        }

        // Case 2: Student is not enrolled in this schedule's class
        if (!isStudentEnrolledInSchedule(currentUser, schedule)) {
            log.debug("Student {} is not enrolled in schedule {}, setting survey status to NONE",
                    userId, schedule.getId());
            return SurveyStatus.NONE;
        }

        // Case 3: Student is enrolled - check if they completed the survey
        Optional<SurveyResponseEntity> responseOpt =
                surveyResponseRepository.findByUserIdAndScheduleId(userId, schedule.getId());

        if (responseOpt.isPresent()) {
            log.debug("Student {} has completed survey for schedule {}", userId, schedule.getId());
            return SurveyStatus.COMPLETED;
        } else {
            log.debug("Student {} has not started survey for schedule {}", userId, schedule.getId());
            return SurveyStatus.NOT_STARTED;
        }
    }

    /**
     * Check if student is enrolled in the schedule's class
     */
    private boolean isStudentEnrolledInSchedule(UserEntity student, ScheduleResponseDto schedule) {
        if (student.getClasses() == null || schedule.getClasses() == null) {
            return false;
        }

        return student.getClasses().getId().equals(schedule.getClasses().getId());
    }

    /**
     * Set survey completion details for completed surveys
     */
    private void setSurveyCompletionDetails(ScheduleResponseDto schedule, Long userId) {
        Optional<SurveyResponseEntity> responseOpt =
                surveyResponseRepository.findByUserIdAndScheduleId(userId, schedule.getId());

        if (responseOpt.isPresent()) {
            SurveyResponseEntity response = responseOpt.get();
            schedule.setSurveySubmittedAt(response.getSubmittedAt());
            schedule.setSurveyResponseId(response.getId());

            log.debug("Set survey completion details for schedule {}: submitted at {}, response ID {}",
                    schedule.getId(), response.getSubmittedAt(), response.getId());
        }
    }

    private void addSurveyStatusToSchedule(ScheduleResponseDto schedule) {
        try {
            UserEntity currentUser = securityUtils.getCurrentUser();

            // Determine survey status based on user role and enrollment
            SurveyStatus status = determineSurveyStatus(currentUser, schedule, currentUser.getId());
            schedule.setSurveyStatus(status);

            // Set submission details based on status
            if (status == SurveyStatus.COMPLETED) {
                setSurveyCompletionDetails(schedule, currentUser.getId());
                log.info("User {} has COMPLETED survey for schedule {}",
                        currentUser.getId(), schedule.getId());
            } else {
                // Clear submission details for non-completed surveys
                schedule.setSurveySubmittedAt(null);
                schedule.setSurveyResponseId(null);

                log.info("User {} survey status for schedule {}: {}",
                        currentUser.getId(), schedule.getId(), status);
            }

        } catch (Exception e) {
            log.debug("Could not determine survey status: {}", e.getMessage());
            // Default to NONE for any errors
            schedule.setSurveyStatus(SurveyStatus.NONE);
            schedule.setSurveySubmittedAt(null);
            schedule.setSurveyResponseId(null);
        }
    }

    private Pageable createSchedulePageable(ScheduleFilterDto filterDto) {
        return PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "day", // Sort by day first, then by start time in specification
                "ASC"
        );
    }

    // Role checking methods
    private boolean hasAdminAccess(UserEntity user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.ADMIN || role.getName() == RoleEnum.DEVELOPER);
    }

    private boolean isTeacherOrStaff(UserEntity user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.TEACHER || role.getName() == RoleEnum.STAFF);
    }

    private boolean isStudent(UserEntity user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.STUDENT);
    }

    // Entity finder methods
    private ScheduleEntity findScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + id));
    }

    private ClassEntity findClassById(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Class not found with ID: " + id));
    }

    private UserEntity findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
    }

    private CourseEntity findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course not found with ID: " + id));
    }

    private RoomEntity findRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found with ID: " + id));
    }

    private SemesterEntity findSemesterById(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Semester not found with ID: " + id));
    }
}