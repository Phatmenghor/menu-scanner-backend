package com.menghor.ksit.feature.school.specification;

import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.SemesterEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.RoomEntity;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.feature.school.model.CourseEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ScheduleSpecification {

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

    public static Specification<ScheduleEntity> hasDayOfWeek(DayOfWeek dayOfWeek) {
        return (root, query, criteriaBuilder) -> {
            if (dayOfWeek == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("day"), dayOfWeek);
        };
    }

    /**
     * Helper method to try parsing a time string in various formats
     */
    private static LocalTime parseTimeIfPossible(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }

        try {
            // Try standard format HH:mm
            return LocalTime.parse(timeString);
        } catch (DateTimeParseException e1) {
            try {
                // Try with formatter for more flexibility
                return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("H:mm"));
            } catch (DateTimeParseException e2) {
                try {
                    // Try with just hours
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

    /**
     * Search schedules by any searchable field
     */
    public static Specification<ScheduleEntity> search(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) return criteriaBuilder.conjunction();

            // Make the query distinct to avoid duplicate results with multiple joins
            query.distinct(true);

            String term = "%" + searchTerm.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // Try to parse the search term as a time
            LocalTime searchTime = parseTimeIfPossible(searchTerm);
            if (searchTime != null) {
                // CORRECT: Use equality comparison for time fields
                predicates.add(criteriaBuilder.equal(root.get("startTime"), searchTime));
                predicates.add(criteriaBuilder.equal(root.get("endTime"), searchTime));

                // You could also add other time comparison options
                // For example, find times close to the search time (within 30 minutes)
                LocalTime thirtyMinsBefore = searchTime.minusMinutes(30);
                LocalTime thirtyMinsAfter = searchTime.plusMinutes(30);

                predicates.add(criteriaBuilder.between(
                        root.get("startTime"),
                        thirtyMinsBefore,
                        thirtyMinsAfter
                ));

                predicates.add(criteriaBuilder.between(
                        root.get("endTime"),
                        thirtyMinsBefore,
                        thirtyMinsAfter
                ));
            }

            // Search in class code
            try {
                Join<ScheduleEntity, ClassEntity> classJoin = root.join("classes", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(classJoin.get("code")), term));
            } catch (IllegalArgumentException e) {
                // Join failed, ignore this part
            }

            // Search in teacher name
            try {
                Join<ScheduleEntity, UserEntity> teacherJoin = root.join("user", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("englishFirstName")), term));
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("englishLastName")), term));
            } catch (IllegalArgumentException e) {
                // Join failed, ignore this part
            }

            // Search in course name
            try {
                Join<ScheduleEntity, CourseEntity> courseJoin = root.join("course", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(courseJoin.get("nameEn")), term));
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(courseJoin.get("nameKH")), term));
            } catch (IllegalArgumentException e) {
                // Join failed, ignore this part
            }

            // Search in room name
            try {
                Join<ScheduleEntity, RoomEntity> roomJoin = root.join("room", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(roomJoin.get("name")), term));
            } catch (IllegalArgumentException e) {
                // Join failed, ignore this part
            }

            // WRONG APPROACH - THIS CAUSES THE ERROR:
            // predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("startTime")), term));
            // predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("endTime")), term));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Combine all specifications for filtering
     */
    public static Specification<ScheduleEntity> createScheduleSpecification(
            String search,
            Long classId,
            Long roomId,
            Long teacherId,
            Integer academyYear,
            SemesterEnum semester,
            Status status,
            DayOfWeek dayOfWeek) {

        Specification<ScheduleEntity> spec = Specification.where(null);

        if (StringUtils.hasText(search)) {
            spec = spec.and(search(search));
        }

        if (classId != null) {
            spec = spec.and(hasClassId(classId));
        }

        if (roomId != null) {
            spec = spec.and(hasRoomId(roomId));
        }

        if (teacherId != null) {
            spec = spec.and(hasTeacherId(teacherId));
        }

        if (academyYear != null) {
            spec = spec.and(hasSemesterAcademyYear(academyYear));
        }

        if (semester != null) {
            spec = spec.and(hasSemesterType(semester));
        }

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }

        if (dayOfWeek != null) {
            spec = spec.and(hasDayOfWeek(dayOfWeek));
        }

        return spec;
    }
}