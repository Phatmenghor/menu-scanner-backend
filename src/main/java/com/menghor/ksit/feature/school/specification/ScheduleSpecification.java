package com.menghor.ksit.feature.school.specification;

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

    /**
     * Search schedules by any searchable field
     */
    public static Specification<ScheduleEntity> search(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) return criteriaBuilder.conjunction();

            String term = "%" + searchTerm.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // Search in schedule fields
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("startTime")), term));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("endTime")), term));

            // Search in class name
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
            Status status) {

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

        return spec;
    }
}