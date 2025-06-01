package com.menghor.ksit.feature.school.specification;

import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.SemesterEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.RoomEntity;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.feature.school.dto.filter.ScheduleFilterDto;
import com.menghor.ksit.feature.school.model.CourseEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ScheduleSpecification {

    /**
     * Create comprehensive specification from filter DTO
     */
    public static Specification<ScheduleEntity> createSpecification(ScheduleFilterDto filterDto, UserRepository userRepository) {
        Specification<ScheduleEntity> spec = Specification.where(null);

        if (filterDto.getClassId() != null) {
            spec = spec.and(hasClassId(filterDto.getClassId()));
        }
        if (filterDto.getRoomId() != null) {
            spec = spec.and(hasRoomId(filterDto.getRoomId()));
        }
        if (filterDto.getTeacherId() != null) {
            spec = spec.and(hasTeacherId(filterDto.getTeacherId()));
        }
        if (filterDto.getStudentId() != null) {
            spec = spec.and(hasStudentId(filterDto.getStudentId(), userRepository));
        }
        if (filterDto.getAcademyYear() != null) {
            spec = spec.and(hasSemesterAcademyYear(filterDto.getAcademyYear()));
        }
        if (filterDto.getSemester() != null) {
            spec = spec.and(hasSemesterType(filterDto.getSemester()));
        }
        if (filterDto.getStatus() != null) {
            spec = spec.and(hasStatus(filterDto.getStatus()));
        }
        if (filterDto.getDayOfWeek() != null) {
            spec = spec.and(hasDayOfWeek(filterDto.getDayOfWeek()));
        }
        if (StringUtils.hasText(filterDto.getSearch())) {
            spec = spec.and(search(filterDto.getSearch()));
        }

        return spec;
    }

    /**
     * Create specification for teacher-specific schedules
     */
    public static Specification<ScheduleEntity> createTeacherSpecification(Long teacherId, ScheduleFilterDto filterDto) {
        Specification<ScheduleEntity> spec = hasTeacherId(teacherId);

        if (filterDto.getClassId() != null) {
            spec = spec.and(hasClassId(filterDto.getClassId()));
        }
        if (filterDto.getRoomId() != null) {
            spec = spec.and(hasRoomId(filterDto.getRoomId()));
        }
        if (filterDto.getAcademyYear() != null) {
            spec = spec.and(hasSemesterAcademyYear(filterDto.getAcademyYear()));
        }
        if (filterDto.getSemester() != null) {
            spec = spec.and(hasSemesterType(filterDto.getSemester()));
        }
        if (filterDto.getStatus() != null) {
            spec = spec.and(hasStatus(filterDto.getStatus()));
        }
        if (filterDto.getDayOfWeek() != null) {
            spec = spec.and(hasDayOfWeek(filterDto.getDayOfWeek()));
        }
        if (StringUtils.hasText(filterDto.getSearch())) {
            spec = spec.and(search(filterDto.getSearch()));
        }

        return spec;
    }

    /**
     * Create specification for student-specific schedules (by class)
     */
    public static Specification<ScheduleEntity> createStudentSpecification(Long classId, ScheduleFilterDto filterDto) {
        Specification<ScheduleEntity> spec = hasClassId(classId);

        if (filterDto.getRoomId() != null) {
            spec = spec.and(hasRoomId(filterDto.getRoomId()));
        }
        if (filterDto.getAcademyYear() != null) {
            spec = spec.and(hasSemesterAcademyYear(filterDto.getAcademyYear()));
        }
        if (filterDto.getSemester() != null) {
            spec = spec.and(hasSemesterType(filterDto.getSemester()));
        }
        if (filterDto.getStatus() != null) {
            spec = spec.and(hasStatus(filterDto.getStatus()));
        }
        if (filterDto.getDayOfWeek() != null) {
            spec = spec.and(hasDayOfWeek(filterDto.getDayOfWeek()));
        }
        if (StringUtils.hasText(filterDto.getSearch())) {
            spec = spec.and(search(filterDto.getSearch()));
        }

        return spec;
    }

    /**
     * Filter schedules by class ID
     */
    public static Specification<ScheduleEntity> hasClassId(Long classId) {
        return (root, query, criteriaBuilder) -> {
            if (classId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("classes").get("id"), classId);
        };
    }

    /**
     * Filter schedules by room ID
     */
    public static Specification<ScheduleEntity> hasRoomId(Long roomId) {
        return (root, query, criteriaBuilder) -> {
            if (roomId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("room").get("id"), roomId);
        };
    }

    /**
     * Filter schedules by teacher ID
     */
    public static Specification<ScheduleEntity> hasTeacherId(Long teacherId) {
        return (root, query, criteriaBuilder) -> {
            if (teacherId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("user").get("id"), teacherId);
        };
    }

    /**
     * Filter schedules by student ID (finds student's class and filters by that)
     */
    public static Specification<ScheduleEntity> hasStudentId(Long studentId, UserRepository userRepository) {
        return (root, query, criteriaBuilder) -> {
            if (studentId == null) return criteriaBuilder.conjunction();

            try {
                UserEntity student = userRepository.findById(studentId).orElse(null);
                if (student == null || student.getClasses() == null) {
                    log.warn("Student with ID {} not found or has no class assigned", studentId);
                    // Return impossible condition to get no results
                    return criteriaBuilder.equal(criteriaBuilder.literal(1), 0);
                }

                log.info("Filtering schedules by student ID: {} (class ID: {})", studentId, student.getClasses().getId());
                return criteriaBuilder.equal(root.get("classes").get("id"), student.getClasses().getId());
            } catch (Exception e) {
                log.error("Error filtering by student ID: {}", studentId, e);
                return criteriaBuilder.equal(criteriaBuilder.literal(1), 0);
            }
        };
    }

    /**
     * Filter schedules by semester's academyYear
     */
    public static Specification<ScheduleEntity> hasSemesterAcademyYear(Integer academyYear) {
        return (root, query, criteriaBuilder) -> {
            if (academyYear == null) return criteriaBuilder.conjunction();
            Join<ScheduleEntity, SemesterEntity> semesterJoin = root.join("semester", JoinType.INNER);
            return criteriaBuilder.equal(semesterJoin.get("academyYear"), academyYear);
        };
    }

    /**
     * Filter schedules by semester type
     */
    public static Specification<ScheduleEntity> hasSemesterType(SemesterEnum semester) {
        return (root, query, criteriaBuilder) -> {
            if (semester == null) return criteriaBuilder.conjunction();
            Join<ScheduleEntity, SemesterEntity> semesterJoin = root.join("semester", JoinType.INNER);
            return criteriaBuilder.equal(semesterJoin.get("semester"), semester);
        };
    }

    /**
     * Filter schedules by status
     */
    public static Specification<ScheduleEntity> hasStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter schedules by day of week
     */
    public static Specification<ScheduleEntity> hasDayOfWeek(DayOfWeek dayOfWeek) {
        return (root, query, criteriaBuilder) -> {
            if (dayOfWeek == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("day"), dayOfWeek);
        };
    }

    /**
     * Search schedules by any searchable field
     */
    public static Specification<ScheduleEntity> search(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) {
                return criteriaBuilder.conjunction();
            }

            // Make the query distinct to avoid duplicate results with multiple joins
            if (query != null) {
                query.distinct(true);
            }

            String term = "%" + searchTerm.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // Try to parse the search term as a time
            LocalTime searchTime = parseTimeIfPossible(searchTerm);
            if (searchTime != null) {
                predicates.add(criteriaBuilder.equal(root.get("startTime"), searchTime));
                predicates.add(criteriaBuilder.equal(root.get("endTime"), searchTime));

                // Also add time range search (within 30 minutes)
                LocalTime thirtyMinsBefore = searchTime.minusMinutes(30);
                LocalTime thirtyMinsAfter = searchTime.plusMinutes(30);
                predicates.add(criteriaBuilder.between(root.get("startTime"), thirtyMinsBefore, thirtyMinsAfter));
                predicates.add(criteriaBuilder.between(root.get("endTime"), thirtyMinsBefore, thirtyMinsAfter));
            }

            // Search in related entities with safe joins
            addClassSearchPredicates(root, criteriaBuilder, term, predicates);
            addTeacherSearchPredicates(root, criteriaBuilder, term, predicates);
            addCourseSearchPredicates(root, criteriaBuilder, term, predicates);
            addRoomSearchPredicates(root, criteriaBuilder, term, predicates);

            return predicates.isEmpty() ? criteriaBuilder.conjunction() :
                    criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addClassSearchPredicates(Root<ScheduleEntity> root, CriteriaBuilder criteriaBuilder,
                                                 String term, List<Predicate> predicates) {
        try {
            Join<ScheduleEntity, ClassEntity> classJoin = root.join("classes", JoinType.LEFT);
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(classJoin.get("code")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(classJoin.get("name")), term));
        } catch (IllegalArgumentException ignored) {
            // Join failed, skip this search
        }
    }

    private static void addTeacherSearchPredicates(Root<ScheduleEntity> root, CriteriaBuilder criteriaBuilder,
                                                   String term, List<Predicate> predicates) {
        try {
            Join<ScheduleEntity, UserEntity> teacherJoin = root.join("user", JoinType.LEFT);
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("englishFirstName")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("englishLastName")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("khmerFirstName")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("khmerLastName")), term));
        } catch (IllegalArgumentException ignored) {
            // Join failed, skip this search
        }
    }

    private static void addCourseSearchPredicates(Root<ScheduleEntity> root, CriteriaBuilder criteriaBuilder,
                                                  String term, List<Predicate> predicates) {
        try {
            Join<ScheduleEntity, CourseEntity> courseJoin = root.join("course", JoinType.LEFT);
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(courseJoin.get("nameEn")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(courseJoin.get("nameKH")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(courseJoin.get("code")), term));
        } catch (IllegalArgumentException ignored) {
            // Join failed, skip this search
        }
    }

    private static void addRoomSearchPredicates(Root<ScheduleEntity> root, CriteriaBuilder criteriaBuilder,
                                                String term, List<Predicate> predicates) {
        try {
            Join<ScheduleEntity, RoomEntity> roomJoin = root.join("room", JoinType.LEFT);
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(roomJoin.get("name")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(roomJoin.get("code")), term));
        } catch (IllegalArgumentException ignored) {
            // Join failed, skip this search
        }
    }

    /**
     * Helper method to try parsing a time string in various formats
     */
    private static LocalTime parseTimeIfPossible(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(timeString);
        } catch (DateTimeParseException e1) {
            try {
                return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("H:mm"));
            } catch (DateTimeParseException e2) {
                try {
                    if (timeString.matches("\\d{1,2}")) {
                        return LocalTime.of(Integer.parseInt(timeString), 0);
                    }
                    return null;
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }
}