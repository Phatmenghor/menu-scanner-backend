package com.menghor.ksit.feature.attendance.specification;

import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.JoinType;

import java.time.LocalDateTime;

public class ScoreSessionSpecification {

    public static Specification<ScoreSessionEntity> hasId(Long id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    public static Specification<ScoreSessionEntity> searchByNameOrCode(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) return criteriaBuilder.conjunction();

            String searchPattern = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("schedule").get("course").get("nameEn")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("schedule").get("course").get("nameKh")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("schedule").get("classes").get("code")), searchPattern)
            );
        };
    }

    public static Specification<ScoreSessionEntity> hasStatus(SubmissionStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<ScoreSessionEntity> hasTeacherId(Long teacherId) {
        return (root, query, criteriaBuilder) -> {
            if (teacherId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("teacher").get("id"), teacherId);
        };
    }

    public static Specification<ScoreSessionEntity> hasScheduleId(Long scheduleId) {
        return (root, query, criteriaBuilder) -> {
            if (scheduleId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("schedule").get("id"), scheduleId);
        };
    }

    public static Specification<ScoreSessionEntity> hasClassId(Long classId) {
        return (root, query, criteriaBuilder) -> {
            if (classId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("schedule").get("classes").get("id"), classId);
        };
    }

    public static Specification<ScoreSessionEntity> hasCourseId(Long courseId) {
        return (root, query, criteriaBuilder) -> {
            if (courseId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("schedule").get("course").get("id"), courseId);
        };
    }

    public static Specification<ScoreSessionEntity> hasStudentId(Long studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId == null) return criteriaBuilder.conjunction();

            var studentScoresJoin = root.join("studentScores", JoinType.INNER);
            return criteriaBuilder.equal(studentScoresJoin.get("student").get("id"), studentId);
        };
    }

    public static Specification<ScoreSessionEntity> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get("status"), SubmissionStatus.REJECTED);
    }

    public static Specification<ScoreSessionEntity> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) return criteriaBuilder.conjunction();
            if (startDate == null) return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
            if (endDate == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
        };
    }

    // Combine multiple specifications
    public static Specification<ScoreSessionEntity> combine(
            String search, SubmissionStatus status, Long teacherId, Long scheduleId,
            Long classId, Long courseId, Long studentId) {

        return Specification
                .where(searchByNameOrCode(search))
                .and(hasStatus(status))
                .and(hasTeacherId(teacherId))
                .and(hasScheduleId(scheduleId))
                .and(hasClassId(classId))
                .and(hasCourseId(courseId))
                .and(hasStudentId(studentId))
                .and(isNotDeleted());
    }
}