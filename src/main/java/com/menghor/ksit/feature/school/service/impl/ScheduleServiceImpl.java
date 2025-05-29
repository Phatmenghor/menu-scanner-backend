package com.menghor.ksit.feature.school.service.impl;

import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.InvalidPaginationException;
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
import com.menghor.ksit.feature.school.mapper.ScheduleMapper;
import com.menghor.ksit.feature.school.model.CourseEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.CourseRepository;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
import com.menghor.ksit.feature.school.service.ScheduleService;
import com.menghor.ksit.feature.school.specification.ScheduleSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    @PersistenceContext
    private EntityManager entityManager;

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
    public CustomPaginationResponseDto<ScheduleResponseDto> getAllSchedules(ScheduleFilterDto filterDto) {
        log.info("Fetching all schedules with filter: {}", filterDto);

        // Create a CriteriaBuilder
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Create a CriteriaQuery for retrieving schedules
        CriteriaQuery<ScheduleEntity> cq = cb.createQuery(ScheduleEntity.class);
        Root<ScheduleEntity> root = cq.from(ScheduleEntity.class);

        // Declare all join variables at the beginning
        Join<ScheduleEntity, ClassEntity> classJoin = null;
        Join<ScheduleEntity, UserEntity> teacherJoin = null;
        Join<ScheduleEntity, CourseEntity> courseJoin = null;
        Join<ScheduleEntity, RoomEntity> roomJoin = null;
        Join<ScheduleEntity, SemesterEntity> semesterJoin = null;

        // Apply filters from the filter DTO
        List<Predicate> predicates = new ArrayList<>();

        if (filterDto.getClassId() != null) {
            classJoin = root.join("classes", JoinType.LEFT);
            predicates.add(cb.equal(classJoin.get("id"), filterDto.getClassId()));
        }

        if (filterDto.getRoomId() != null) {
            roomJoin = root.join("room", JoinType.LEFT);
            predicates.add(cb.equal(roomJoin.get("id"), filterDto.getRoomId()));
        }

        if (filterDto.getTeacherId() != null) {
            teacherJoin = root.join("user", JoinType.LEFT);
            predicates.add(cb.equal(teacherJoin.get("id"), filterDto.getTeacherId()));
        }

        // Create semester join only once if either condition needs it
        if (filterDto.getAcademyYear() != null || filterDto.getSemester() != null) {
            semesterJoin = root.join("semester", JoinType.INNER);

            if (filterDto.getAcademyYear() != null) {
                predicates.add(cb.equal(semesterJoin.get("academyYear"), filterDto.getAcademyYear()));
            }

            if (filterDto.getSemester() != null) {
                predicates.add(cb.equal(semesterJoin.get("semester"), filterDto.getSemester()));
            }
        }

        if (filterDto.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), filterDto.getStatus()));
        }

        if (filterDto.getDayOfWeek() != null) {
            predicates.add(cb.equal(root.get("day"), filterDto.getDayOfWeek()));
        }

        // Add search criteria if provided - with careful null handling
        if (filterDto.getSearch() != null && !filterDto.getSearch().trim().isEmpty()) {
            // Safely handle the search term
            String searchTerm = "%" + filterDto.getSearch().toLowerCase().trim().replace("'", "''") + "%";

            // Initialize joins safely
            if (classJoin == null) {
                classJoin = root.join("classes", JoinType.LEFT);
            }
            if (teacherJoin == null) {
                teacherJoin = root.join("user", JoinType.LEFT);
            }

            if (courseJoin == null) {
                courseJoin = root.join("course", JoinType.LEFT);
            }

            if (roomJoin == null) {
                roomJoin = root.join("room", JoinType.LEFT);
            }

            // Build search predicates carefully
            List<Predicate> searchPredicates = new ArrayList<>();

            searchPredicates.add(cb.like(cb.lower(classJoin.get("code")), searchTerm));
            searchPredicates.add(cb.like(cb.lower(teacherJoin.get("englishFirstName")), searchTerm));
            searchPredicates.add(cb.like(cb.lower(teacherJoin.get("englishLastName")), searchTerm));
            searchPredicates.add(cb.like(cb.lower(courseJoin.get("nameEn")), searchTerm));
            searchPredicates.add(cb.like(cb.lower(courseJoin.get("nameKH")), searchTerm));
            searchPredicates.add(cb.like(cb.lower(roomJoin.get("name")), searchTerm));

            predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
        }

        // Apply all predicates to the query
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Create a CASE expression for day of week ordering
        Expression<Integer> dayOrderCase = cb.<Integer>selectCase()
                .when(cb.equal(root.get("day"), DayOfWeek.MONDAY), 1)
                .when(cb.equal(root.get("day"), DayOfWeek.TUESDAY), 2)
                .when(cb.equal(root.get("day"), DayOfWeek.WEDNESDAY), 3)
                .when(cb.equal(root.get("day"), DayOfWeek.THURSDAY), 4)
                .when(cb.equal(root.get("day"), DayOfWeek.FRIDAY), 5)
                .when(cb.equal(root.get("day"), DayOfWeek.SATURDAY), 6)
                .when(cb.equal(root.get("day"), DayOfWeek.SUNDAY), 7)
                .otherwise(8); // null or other values

        // Order by day of week and then by start time
        cq.orderBy(cb.asc(dayOrderCase), cb.asc(root.get("startTime")));

        // Create a count query for pagination
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ScheduleEntity> countRoot = countQuery.from(ScheduleEntity.class);

        // Create the same joins for the count query if predicates need them
        Join<ScheduleEntity, ClassEntity> countClassJoin = null;
        Join<ScheduleEntity, UserEntity> countTeacherJoin = null;
        Join<ScheduleEntity, CourseEntity> countCourseJoin = null;
        Join<ScheduleEntity, RoomEntity> countRoomJoin = null;
        Join<ScheduleEntity, SemesterEntity> countSemesterJoin = null;

        // Create the same join structure for the count query that we used in the main query
        if (classJoin != null) {
            countClassJoin = countRoot.join("classes", JoinType.LEFT);
        }

        if (roomJoin != null) {
            countRoomJoin = countRoot.join("room", JoinType.LEFT);
        }

        if (teacherJoin != null) {
            countTeacherJoin = countRoot.join("user", JoinType.LEFT);
        }

        if (semesterJoin != null) {
            countSemesterJoin = countRoot.join("semester", JoinType.INNER);
        }

        if (courseJoin != null) {
            countCourseJoin = countRoot.join("course", JoinType.LEFT);
        }

        countQuery.select(cb.count(countRoot));

        // Rebuild predicates list for count query
        List<Predicate> countPredicates = new ArrayList<>();

        if (filterDto.getClassId() != null && countClassJoin != null) {
            countPredicates.add(cb.equal(countClassJoin.get("id"), filterDto.getClassId()));
        }

        if (filterDto.getRoomId() != null && countRoomJoin != null) {
            countPredicates.add(cb.equal(countRoomJoin.get("id"), filterDto.getRoomId()));
        }

        if (filterDto.getTeacherId() != null && countTeacherJoin != null) {
            countPredicates.add(cb.equal(countTeacherJoin.get("id"), filterDto.getTeacherId()));
        }

        if (countSemesterJoin != null) {
            if (filterDto.getAcademyYear() != null) {
                countPredicates.add(cb.equal(countSemesterJoin.get("academyYear"), filterDto.getAcademyYear()));
            }

            if (filterDto.getSemester() != null) {
                countPredicates.add(cb.equal(countSemesterJoin.get("semester"), filterDto.getSemester()));
            }
        }

        if (filterDto.getStatus() != null) {
            countPredicates.add(cb.equal(countRoot.get("status"), filterDto.getStatus()));
        }

        if (filterDto.getDayOfWeek() != null) {
            countPredicates.add(cb.equal(countRoot.get("day"), filterDto.getDayOfWeek()));
        }

        // Rebuild search predicate for count query - with careful null handling
        if (filterDto.getSearch() != null && !filterDto.getSearch().trim().isEmpty()) {
            String searchTerm = "%" + filterDto.getSearch().toLowerCase().trim().replace("'", "''") + "%";

            List<Predicate> countSearchPredicates = new ArrayList<>();

            if (countClassJoin != null) {
                countSearchPredicates.add(cb.like(cb.lower(countClassJoin.get("code")), searchTerm));
            }

            if (countTeacherJoin != null) {
                countSearchPredicates.add(cb.like(cb.lower(countTeacherJoin.get("englishFirstName")), searchTerm));
                countSearchPredicates.add(cb.like(cb.lower(countTeacherJoin.get("englishLastName")), searchTerm));
            }

            if (countCourseJoin != null) {
                countSearchPredicates.add(cb.like(cb.lower(countCourseJoin.get("nameEn")), searchTerm));
                countSearchPredicates.add(cb.like(cb.lower(countCourseJoin.get("nameKH")), searchTerm));
            }

            if (countRoomJoin != null) {
                countSearchPredicates.add(cb.like(cb.lower(countRoomJoin.get("name")), searchTerm));
            }

            if (!countSearchPredicates.isEmpty()) {
                countPredicates.add(cb.or(countSearchPredicates.toArray(new Predicate[0])));
            }
        }

        // Apply predicates to count query
        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }

        // Execute the count query
        Long totalResults = entityManager.createQuery(countQuery).getSingleResult();

        // Apply pagination with appropriate default values
        int pageNo = filterDto.getPageNo() != null && filterDto.getPageNo() > 0 ? filterDto.getPageNo() : 1;
        int pageSize = filterDto.getPageSize() != null && filterDto.getPageSize() > 0 ? filterDto.getPageSize() : 10;
        int zeroBasedPageNo = pageNo - 1;

        TypedQuery<ScheduleEntity> query = entityManager.createQuery(cq);
        query.setFirstResult(zeroBasedPageNo * pageSize);
        query.setMaxResults(pageSize);

        try {
            // Execute the query with error handling
            List<ScheduleEntity> schedules = query.getResultList();

            // Create a Page object
            Page<ScheduleEntity> page = new PageImpl<>(
                    schedules,
                    PageRequest.of(zeroBasedPageNo, pageSize),
                    totalResults
            );

            // Convert to response DTO with error handling
            CustomPaginationResponseDto<ScheduleResponseDto> response = scheduleMapper.toScheduleAllResponseDto(page);

            log.info("Retrieved {} schedules (page {}/{})",
                    response.getContent().size(),
                    response.getPageNo(),
                    response.getTotalPages());

            return response;
        } catch (Exception e) {
            log.error("Error processing schedule query results: " + e.getMessage(), e);
            throw new RuntimeException("Error processing schedule results: " + e.getMessage(), e);
        }
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

        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new NotFoundException("User not found with ID: " + id);
                });
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