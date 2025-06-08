package com.menghor.ksit.feature.school.service.impl;

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
import com.menghor.ksit.feature.school.dto.request.ScheduleRequestDto;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
    private final SurveyService surveyService;

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

        // Add survey status to each schedule for current user (if user is a student)
        try {
            UserEntity currentUser = securityUtils.getCurrentUser();
            if (isStudent(currentUser)) {
                addSurveyStatusToSchedules(response.getContent(), currentUser.getId());
            }
        } catch (Exception e) {
            log.debug("Could not determine current user or add survey status: {}", e.getMessage());
        }

        log.info("Retrieved {} schedules (page {}/{})",
                response.getContent().size(), response.getPageNo(), response.getTotalPages());

        return response;
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

    // ===== Private Helper Methods =====

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
        log.info("Retrieved {} schedules for teacher (page {}/{})",
                response.getContent().size(), response.getPageNo(), response.getTotalPages());

        return response;
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

        // Add survey status for student
        addSurveyStatusToSchedules(response.getContent(), student.getId());

        log.info("Retrieved {} schedules for student (page {}/{})",
                response.getContent().size(), response.getPageNo(), response.getTotalPages());

        return response;
    }

    private void addSurveyStatusToSchedules(List<ScheduleResponseDto> schedules, Long userId) {
        for (ScheduleResponseDto schedule : schedules) {
            try {
                // Check if user has completed survey for this schedule
                Boolean hasCompleted = surveyService.hasUserCompletedSurvey(userId, schedule.getId());

                if (hasCompleted) {
                    schedule.setSurveyStatus(SurveyStatus.COMPLETED);

                    // Get survey response details
                    Optional<com.menghor.ksit.feature.survey.model.SurveyResponseEntity> responseOpt =
                            surveyResponseRepository.findByUserIdAndScheduleId(userId, schedule.getId());

                    if (responseOpt.isPresent()) {
                        var response = responseOpt.get();
                        schedule.setSurveySubmittedAt(response.getSubmittedAt());
                        schedule.setSurveyResponseId(response.getId());
                    }
                } else {
                    schedule.setSurveyStatus(SurveyStatus.NOT_STARTED);
                    schedule.setSurveySubmittedAt(null);
                    schedule.setSurveyResponseId(null);
                }

                schedule.setHasSurvey(true);

            } catch (Exception e) {
                log.debug("Error checking survey status for schedule {}: {}", schedule.getId(), e.getMessage());
                schedule.setSurveyStatus(SurveyStatus.NOT_STARTED);
                schedule.setHasSurvey(false);
            }
        }
    }

    private void addSurveyStatusToSchedule(ScheduleResponseDto schedule) {
        try {
            UserEntity currentUser = securityUtils.getCurrentUser();
            if (isStudent(currentUser)) {
                // Check if user has completed survey for this schedule
                Boolean hasCompleted = surveyService.hasUserCompletedSurvey(currentUser.getId(), schedule.getId());

                if (hasCompleted) {
                    schedule.setSurveyStatus(SurveyStatus.COMPLETED);

                    // Get survey response details
                    Optional<com.menghor.ksit.feature.survey.model.SurveyResponseEntity> responseOpt =
                            surveyResponseRepository.findByUserIdAndScheduleId(currentUser.getId(), schedule.getId());

                    if (responseOpt.isPresent()) {
                        var response = responseOpt.get();
                        schedule.setSurveySubmittedAt(response.getSubmittedAt());
                        schedule.setSurveyResponseId(response.getId());
                    }
                } else {
                    schedule.setSurveyStatus(SurveyStatus.NOT_STARTED);
                }

                schedule.setHasSurvey(true);
            }
        } catch (Exception e) {
            log.debug("Could not add survey status: {}", e.getMessage());
            schedule.setSurveyStatus(SurveyStatus.NOT_STARTED);
            schedule.setHasSurvey(false);
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