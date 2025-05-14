package com.menghor.ksit.feature.school.service.impl;

import com.menghor.ksit.enumations.Status;
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
import com.menghor.ksit.feature.school.dto.response.ScheduleResponseListDto;
import com.menghor.ksit.feature.school.dto.update.ScheduleUpdateDto;
import com.menghor.ksit.feature.school.mapper.ScheduleMapper;
import com.menghor.ksit.feature.school.model.CourseEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.CourseRepository;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
import com.menghor.ksit.feature.school.service.ScheduleService;
import com.menghor.ksit.feature.school.specification.ScheduleSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
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
    private final ScheduleMapper scheduleMapper;

    @Override
    @Transactional
    public ScheduleResponseDto createSchedule(ScheduleRequestDto requestDto) {
        log.info("Creating new schedule for class ID: {}, day: {}, time: {}-{}", 
                requestDto.getClassId(), requestDto.getDay(), requestDto.getStartTime(), requestDto.getEndTime());

        // Validate all required relationships
        ClassEntity classEntity = findClassById(requestDto.getClassId());
        UserEntity teacher = findTeacherById(requestDto.getTeacherId());
        CourseEntity course = findCourseById(requestDto.getCourseId());
        RoomEntity room = findRoomById(requestDto.getRoomId());
        SemesterEntity semester = findSemesterById(requestDto.getSemesterId());

        // Create new schedule entity
        ScheduleEntity schedule = scheduleMapper.toEntity(requestDto);
        
        // Set relationships
        schedule.setClasses(classEntity);
        schedule.setUser(teacher);
        schedule.setCourse(course);
        schedule.setRoom(room);
        schedule.setSemester(semester);
        
        // Set status if not provided
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
        log.info("Retrieved schedule with ID: {}", id);

        return scheduleMapper.toResponseDto(schedule);
    }

    @Override
    @Transactional
    public ScheduleResponseDto updateSchedule(Long id, ScheduleUpdateDto updateDto) {
        log.info("Updating schedule with ID: {}", id);

        // Find existing schedule
        ScheduleEntity existingSchedule = findScheduleById(id);

        // Use MapStruct to update fields (ignoring relationship fields)
        scheduleMapper.updateEntityFromDto(updateDto, existingSchedule);

        // Update relationships if provided
        if (updateDto.getClassId() != null) {
            ClassEntity classEntity = findClassById(updateDto.getClassId());
            existingSchedule.setClasses(classEntity);
        }

        if (updateDto.getTeacherId() != null) {
            UserEntity teacher = findTeacherById(updateDto.getTeacherId());
            existingSchedule.setUser(teacher);
        }

        if (updateDto.getCourseId() != null) {
            CourseEntity course = findCourseById(updateDto.getCourseId());
            existingSchedule.setCourse(course);
        }

        if (updateDto.getRoomId() != null) {
            RoomEntity room = findRoomById(updateDto.getRoomId());
            existingSchedule.setRoom(room);
        }

        if (updateDto.getSemesterId() != null) {
            SemesterEntity semester = findSemesterById(updateDto.getSemesterId());
            existingSchedule.setSemester(semester);
        }

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
    public CustomPaginationResponseDto<ScheduleResponseListDto> getAllSchedules(ScheduleFilterDto filterDto) {
        log.info("Fetching all schedules with filter: {}", filterDto);

        // Validate and prepare pagination
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Create specification from filter criteria
        Specification<ScheduleEntity> spec = ScheduleSpecification.createScheduleSpecification(
                filterDto.getSearch(),
                filterDto.getClassId(),
                filterDto.getRoomId(),
                filterDto.getTeacherId(),
                filterDto.getAcademyYear(),
                filterDto.getSemester(),
                filterDto.getStatus()
        );

        // Execute query with specification and pagination
        Page<ScheduleEntity> schedulePage = scheduleRepository.findAll(spec, pageable);

        // Map to response DTO
        CustomPaginationResponseDto<ScheduleResponseListDto> response = scheduleMapper.toScheduleAllResponseDto(schedulePage);
        log.info("Retrieved {} schedules (page {}/{})",
                response.getContent().size(),
                response.getPageNo(),
                response.getTotalPages());

        return response;
    }

    @Override
    public List<ScheduleResponseDto> getSchedulesByClassId(Long classId) {
        log.info("Fetching schedules for class ID: {}", classId);

        // Check if class exists
        findClassById(classId);

        List<ScheduleEntity> schedules = scheduleRepository.findByClassesId(classId);
        
        log.info("Retrieved {} schedules for class ID: {}", schedules.size(), classId);
        
        return schedules.stream()
                .map(scheduleMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to find a schedule by ID or throw NotFoundException
     */
    private ScheduleEntity findScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Schedule not found with ID: {}", id);
                    return new NotFoundException("Schedule not found with ID: " + id);
                });
    }

    /**
     * Helper method to find a class by ID or throw NotFoundException
     */
    private ClassEntity findClassById(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Class not found with ID: {}", id);
                    return new NotFoundException("Class not found with ID: " + id);
                });
    }

    /**
     * Helper method to find a teacher by ID or throw NotFoundException
     */
    private UserEntity findTeacherById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new NotFoundException("User not found with ID: " + id);
                });

        return user;
    }

    /**
     * Helper method to find a course by ID or throw NotFoundException
     */
    private CourseEntity findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", id);
                    return new NotFoundException("Course not found with ID: " + id);
                });
    }

    /**
     * Helper method to find a room by ID or throw NotFoundException
     */
    private RoomEntity findRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Room not found with ID: {}", id);
                    return new NotFoundException("Room not found with ID: " + id);
                });
    }

    /**
     * Helper method to find a semester by ID or throw NotFoundException
     */
    private SemesterEntity findSemesterById(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Semester not found with ID: {}", id);
                    return new NotFoundException("Semester not found with ID: " + id);
                });
    }
}