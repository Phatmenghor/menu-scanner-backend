package com.menghor.ksit.feature.attendance.specification;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.attendance.models.AttendanceSessionEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class AttendanceSessionSpecification {

    public static Specification<AttendanceSessionEntity> hasTeacherId(Long teacherId) {
        return (root, query, criteriaBuilder) -> {
            if (teacherId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("teacher").get("id"), teacherId);
        };
    }

    public static Specification<AttendanceSessionEntity> hasScheduleId(Long scheduleId) {
        return (root, query, criteriaBuilder) -> {
            if (scheduleId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("schedule").get("id"), scheduleId);
        };
    }

    public static Specification<AttendanceSessionEntity> hasClassId(Long classId) {
        return (root, query, criteriaBuilder) -> {
            if (classId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("schedule").get("classes").get("id"), classId);
        };
    }

    public static Specification<AttendanceSessionEntity> hasCourseId(Long courseId) {
        return (root, query, criteriaBuilder) -> {
            if (courseId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("schedule").get("course").get("id"), courseId);
        };
    }

    public static Specification<AttendanceSessionEntity> hasStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<AttendanceSessionEntity> isFinal(Boolean isFinal) {
        return (root, query, criteriaBuilder) -> {
            if (isFinal == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isFinal"), isFinal);
        };
    }

    public static Specification<AttendanceSessionEntity> sessionDateBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) -> {
            if (start == null && end == null) {
                return criteriaBuilder.conjunction();
            }
            if (start == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("sessionDate"), end);
            }
            if (end == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("sessionDate"), start);
            }
            return criteriaBuilder.between(root.get("sessionDate"), start, end);
        };
    }
}